# sqlite/zxjdbc.py
# Copyright (C) 2005-2012 the SQLAlchemy authors and contributors <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: http://www.opensource.org/licenses/mit-license.php

"""Support for the SqlLite3 database via the zxjdbc JDBC connector.

JDBC Driver
-----------

Xerial provides a SqlLite driver that works with zxJDBC. See: http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC .

"""
from sqlalchemy.connectors.zxJDBC import ZxJDBCConnector
from sqlalchemy.dialects.sqlite.base import SQLiteDialect, SQLiteExecutionContext
from com.ziclix.python.sql import zxJDBC
from sqlalchemy import util, exc, pool
import os

class SQLite_zxJDBC_DBAPI(zxJDBC):
    def __init__(self, *args, **kwargs):
        super(SQLite_xzJDBC_DBAPI, self).__init__(*args, **kwargs)

    @property
    def sqlite_version_info(self):
        db = zxJDBC.connect("jdbc:sqlite::memory:", None, None, "org.sqlite.JDBC")
        version = tuple([int(version_part) for version_part in db.dbversion])
        return version

class SQLiteExecutionContext_zxjdbc(SQLiteExecutionContext):

    def create_cursor(self):
        cursor = self._dbapi_connection.cursor()
        cursor.datahandler = self.dialect.DataHandler(cursor.datahandler)
        return cursor


class SQLiteDialect_zxjdbc(ZxJDBCConnector, SQLiteDialect):
    jdbc_db_name = 'sqlite'
    jdbc_driver_name = 'org.sqlite.JDBC'

    #execution_ctx_cls = SQLiteExecutionContext_zxjdbc
    execution_ctx_cls = SQLiteExecutionContext

    supports_native_decimal = False
    #dbapi = zxjdbc_dbapi

    @classmethod
    def dbapi(cls):
        return SQLite_zxJDBC_DBAPI

    def __init__(self, *args, **kwargs):
        ZxJDBCConnector.__init__(self, *args, **kwargs)
        SQLiteDialect.__init__(self, *args, **kwargs)

        #from com.ziclix.python.sql.handler import PostgresqlDataHandler
        #self.DataHandler = PostgresqlDataHandler

    @classmethod
    def get_pool_class(cls, url):
        if url.database and url.database != ':memory:':
            return pool.NullPool
        else:
            return pool.SingletonThreadPool

    def _get_server_version_info(self, connection):
        return tuple(int(x) for x in connection.connection.dbversion.split('.'))

    def create_connect_args(self, url):
        if url.username or url.password or url.host or url.port:
            raise exc.ArgumentError(
                "Invalid SQLite URL: %s\n"
                "Valid SQLite URL forms are:\n"
                " sqlite:///:memory: (or, sqlite://)\n"
                " sqlite:///relative/path/to/file.db\n"
                " sqlite:////absolute/path/to/file.db" % (url,))
        filename = url.database or ':memory:'
        if filename != ':memory:':
            filename = os.path.abspath(filename)

        opts = self._driver_kwargs()
        opts.update(url.query)

        util.coerce_kw_type(opts, 'timeout', float)
        util.coerce_kw_type(opts, 'isolation_level', str)
        util.coerce_kw_type(opts, 'detect_types', int)
        util.coerce_kw_type(opts, 'check_same_thread', bool)
        util.coerce_kw_type(opts, 'cached_statements', int)

        jdbc_url = "jdbc:sqlite:%s" % filename
        return [
            [
                jdbc_url,
                url.username,
                url.password,
                self.jdbc_driver_name
            ],
            opts
        ]

dialect = SQLiteDialect_zxjdbc