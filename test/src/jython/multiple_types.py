from sqlalchemy.orm import relation
from sqlalchemy.schema import Column
from sqlalchemy.types import  *



def init(Base):
    class MultipleTypes(Base):
        __tablename__ = 'types'

        aString = Column(String, primary_key=True)

        aInteger          = Column(Integer     )
        aSmallInteger     = Column(SmallInteger)
        aBigInteger       = Column(BigInteger  )
        aNumeric          = Column(Numeric  ,info={"precision":2}   )
        aFloat            = Column(Float       )
        aDateTime         = Column(DateTime    )
        aDate             = Column(Date        )
        aTime             = Column(Time        )
        aLargeBinary      = Column(LargeBinary )
        aBoolean          = Column(Boolean     )
        aUnicode          = Column(Unicode     )
        aUnicodeText      = Column(UnicodeText )
        aInterval         = Column(Interval    )
        #aEnum             = Column(Enum        )

    return MultipleTypes