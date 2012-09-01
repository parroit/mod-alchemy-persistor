from datetime import datetime
from decimal import Decimal
import sys
from java.io import File
from java.math import BigDecimal,MathContext
import imp


from org.vertx.java.core.json import JsonObject, JsonArray
import cgi
import traceback
from core.event_bus import EventBus
from org.vertx.java.deploy.impl import VertxLocator




import vertx
from sqlalchemy.ext.declarative import declarative_base, DeclarativeMeta
from sqlalchemy.orm.attributes import InstrumentedAttribute
from sqlalchemy.orm.session import sessionmaker
from sqlalchemy import create_engine


class AlchemyPersistor:




    def __init__(self, db_path, address, eventBus, base):
        #remote_debugger.breakpoint()
        self.db_path = db_path
        self.address = address
        self.eventBus = eventBus
        self.base = base
        self.page_len = 30
        self.metadata = base.metadata
        print(File(vertx.config()["model-path"]).getAbsolutePath())
        for py_file in File(vertx.config()["model-path"]).listFiles():
            print (py_file.getAbsolutePath().lower())
            if str(py_file.getAbsolutePath()).lower().endswith(".py"):
                module=imp.load_source(py_file.getName(), py_file.getAbsolutePath())
                if hasattr(module,"init"):
                    clazz = module.init(Base)
                    print("load %s in %s" % (clazz,clazz.__name__) )
                    setattr(self,clazz.__name__, clazz)




    def create_new_session(self):
        return  self.Session(bind=self.engine.connect())


    def start(self):
        #self.eventBus.registerHandler(self.address, self);
        self.config = VertxLocator.container.getConfig()
        self.engine = create_engine(self.db_path, echo=False)

        self.Session = sessionmaker(bind=self.engine)

        EventBus.register_handler(self.address, handler=self.handle)

        self.metadata.create_all(self.engine)


    def send_error(self, message, error):
        self.send_status("error", message, JsonObject().putString("error", error))

    def send_ok(self, message, json):
        self.send_status("ok", message, json)

    def send_status(self, status, message, json):
        if json is None:
            json = JsonObject()
#        if message.time_out:
#            vertx.cancel_timer(message.time_out)
#            message.time_out = None

        json.putString("status", status)
        message.reply(json)


    def stop(self):
        self.engine.disconnect()

    def handle(self, msg):
        session = None
        try:
            action = msg.body["action"]
            session = self.create_new_session()

#            def timeout_handle(error):
#                VertxLocator.container.getLogger().warn("Closing DB cursor on timeout." + str(error))
#                try:
#                    session.close()
#                except ():
#                    pass
#                self.send_error(msg, "timeout error:" + str(error))
#
#            msg.time_out = vertx.set_timer(10000,timeout_handle)
#            msg.time_out = None

            if action is None:
                self.send_error(msg, "action must be specified")
                return

            action_handler = {
                'save': lambda msg: self._doSave(msg),
                'find': lambda msg: self._doFind(msg),
                'findone': lambda msg: self._doFindOne(msg),
                'delete': lambda msg: self._doDelete(msg),
                }.get(action, lambda msg: self.send_error(msg, "Invalid action: " + action))

            action_handler(msg)

        except Exception:
            self.send_error(msg, "persistence error:" + cgi.escape(str(sys.exc_info())).replace("'", "\\\""))
        finally:
            if session:
                session.close()


    def _doSave(self, message):
        collection = self.getMandatoryString("collection", message)
        if collection is None:
            return

        doc = self.getMandatoryObject("document", message)
        if doc is None:
            return

        session = self.create_new_session()

        table = getattr(self, collection)

        import inspect

        fields = inspect.getmembers(table,
            predicate=lambda x: isinstance(x, InstrumentedAttribute))

        fields_vars = [vars(y.property.columns[0]) for x, y in fields if hasattr(y.property,"columns")]

        #doc2 = JsonObject()
        primary_fields = [x['name'] + " == '" + doc[x['name']] + "'" for x in fields_vars if x['primary_key']]

        where = " and ".join(primary_fields)
        print(where)
        actual_doc = session.query(table).filter(where).first()
        print("actual_doc is : " + str(actual_doc))
        print("new doc is : " + str(doc))

        if actual_doc is None:
            actual_doc = table()
            session.add(actual_doc)

        for name, value in doc.iteritems():
            if str(table.__table__.columns[name].type)=="DATETIME":
                value=datetime.strptime(value, "%Y-%m-%d %H:%M:%S")
            elif str(table.__table__.columns[name].type)=="DATE":
                value=datetime.strptime(value, "%Y-%m-%d")
            elif str(table.__table__.columns[name].type)=="TIME":
                value=datetime.strptime(value, "%H:%M:%S").time()
            elif str(table.__table__.columns[name].type)=="NUMERIC":
                value=str(value)
                print("Numeric value is "+value)

            setattr(actual_doc, name, value)

        print("COMMIT: " + str(actual_doc))
        session.commit()
        saved_doc = session.query(table).filter(where).first()

        reply = JsonObject().putObject("document", self.to_json(saved_doc,collection))
        session.close()
        self.send_ok(message, reply)


    def _field(self, message, name):
        if message.body.has_key(name):
            return message.body[name]
        else:
            return None

    def get_matcher(self, message):
        matcher = self.getMandatoryObject("matcher", message)
        if isinstance(matcher, dict):
            matcher = " and ".join([name + " == '" + value + "'" for name, value in matcher.iteritems()])
            print "Converted matcher:" + matcher
        return matcher

    def _doFind(self, message):
        #print("_doFind start")
        collection = self.getMandatoryString("collection", message)
        if collection is None:
            return

        limit = self._field(message, "limit")
        if limit is None:
            limit = -1

        matcher = self.get_matcher(message)
        if matcher is None:
            return

        page = self.getMandatoryObject("page", message)
        if page is None:
            return

        sort = self._field(message, "sort")
        session = self.create_new_session()

        rows = session.query(getattr(self, collection)).filter(matcher)
        total_rows = rows.count()
        rows = rows.order_by(sort).limit(limit).offset(limit * (page - 1))
        pages = int(total_rows / self.page_len)
        if total_rows % self.page_len > 0:
            pages += 1

        self._sendResults(message, rows, pages, total_rows, session)


    def _sendResults(self, message, rows, pages, total_rows, session):
        count = 0


        #Set a timeout, if the user doesn't reply within 10 secs, close the cursor

        first_result = None
        collection = self.getMandatoryString("collection", message)

        results = JsonArray()
        for obj in rows.all():
            json = self.to_json(obj,collection)
            if first_result is None:
                first_result = json
            results.addObject(json)

            count += 1

        reply = JsonObject().putArray("rows", results).putNumber("pages", pages).putNumber("total_rows", total_rows)
        if not first_result is None:
            reply.putObject("result", first_result)

        session.close()
        self.send_ok(message, reply)


    def to_json(self, obj,collection):
        try:
            json = JsonObject()
            for name, type in vars(getattr(self, obj.__class__.__name__)).iteritems():
                if str(type.__class__) == "<class 'sqlalchemy.orm.attributes.InstrumentedAttribute'>":
                    column_value = getattr(obj, name)

                    if column_value:
                        column_name = name

                        class__ = column_value.__class__

                        table = getattr(self, collection)

                        column_type =str(table.__table__.columns[name].type)

                        print ("and type is... "+column_type)

                        if isinstance(class__, DeclarativeMeta):
                            json.putObject(column_name, self.to_json(getattr(obj, name),column_type))

                        elif column_type == "BOOLEAN":
                            json.putBoolean(column_name, getattr(obj, column_name))

                        elif column_type in ["<class 'sqlalchemy.orm.collections.InstrumentedList'>"]:
                            print("A LIST")
                            array = JsonArray()
                            for instance in column_value:
                                print(str(instance))
                                array.addObject(self.to_json(instance,column_type))
                            json.putArray(column_name, array)
                            print("A LIST COMPLETE")

                        elif column_type == "DATETIME":
                            attr = getattr(obj, column_name).strftime("%Y-%m-%d %H:%M:%S")
                            json.putString(column_name,attr)

                        elif column_type == "TIME":
                            attr = getattr(obj, column_name).strftime("%H:%M:%S")
                            json.putString(column_name,attr)

                        elif column_type == "DATE":
                            attr = getattr(obj, column_name).strftime("%Y-%m-%d")
                            json.putString(column_name,attr)

                        elif column_type in ["NUMERIC","FLOAT"]:
                            attr = getattr(obj, column_name)

                            column = table.__table__.columns[name]
                            if not hasattr(column,"info"):
                                precision = 2
                            elif not column.info.has_key("precision"):
                                precision = 2
                            else:
                                precision = column.info["precision"]

                            attr=BigDecimal(str(attr)).setScale(precision, 5)  #BigDecimal.ROUND_HALF_DOWN=5
                            json.putNumber(column_name, attr)

                        elif column_type in ["SMALLINT","BIGINT","INTEGER","NUMERIC"]:
                            attr = getattr(obj, column_name)
                            json.putNumber(column_name, attr)

                        else:
                            json.putString(column_name, getattr(obj, column_name))

            print ("JSON:" + str(json))
            return json
        except Exception:
            exc = str(sys.exc_info()[0])+"\n"+str(sys.exc_info()[1])+"\n"+traceback.format_exc(sys.exc_info()[2])
            print exc
            return None


    def _doFindOne(self, message):
        collection = self.getMandatoryString("collection", message)
        if collection is None:
            return

        matcher = self.get_matcher(message)
        if matcher is None:
            return

        session = self.create_new_session()
        rows = session.query(getattr(self, collection)).filter(matcher)

        self._sendResults(message, rows, 1, 1, session)


    def _doDelete(self, message):
        print("delete start")
        collection = self.getMandatoryString("collection", message)
        if collection is None:
            return

        matcher = self.getMandatoryObject("matcher", message)
        if matcher is None:
            return

        session = self.create_new_session()
        rows = session.query(getattr(self, collection)).filter(matcher)
        for r in rows:
            session.delete(r)
        session.commit()
        session.close()
        self.send_ok(message, JsonObject())

    def getMandatoryObject(self, field, message):
        val = self._field(message, field)
        if val is None:
            self.send_error(message, field + " must be specified")

        return val

    def getMandatoryString(self, field, message):
        val = self._field(message, field)
        if val is None:
            self.send_error(message, field + " must be specified")

        return val


Base = declarative_base()
cfg=VertxLocator.container.getConfig()

address = cfg.getString("address","alchemy-persistor")
protocol =cfg.getString("protocol","sqlite")
host = cfg.getString("host","localhost")
dbName = cfg.getString("db_name")
username = cfg.getString("username")
password = cfg.getString("password")

db_path = str(protocol) + "://"+str(host)+str(dbName)


persistor=AlchemyPersistor(db_path,address,EventBus,Base)
persistor.start()
