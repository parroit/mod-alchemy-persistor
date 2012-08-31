from sqlalchemy.orm import relation
from sqlalchemy.schema import Column
from sqlalchemy.types import  String, Boolean
#
#'String', 'Integer', 'SmallInteger', 'BigInteger', 'Numeric',
#'Float', 'DateTime', 'Date', 'Time', 'LargeBinary', 'Binary',
#'Boolean', 'Unicode', 'MutableType', 'Concatenable',
#'UnicodeText','PickleType', 'Interval', 'Enum' ]

def init(Base):
    class MultipleTypes(Base):
        __tablename__ = 'types'

        aString = Column(String, primary_key=True)
        fullname = Column(String)
        password = Column(String)
        email = Column(String)
        confirmed = Column(Boolean)
        admin = Column(Boolean)


    return MultipleTypes