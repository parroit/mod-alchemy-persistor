#
#Copyright 2012 Andrea Parodi.
#
#Licensed under the Apache License, Version 2.0 (the "License");
#you may not use this file except in compliance with the License.
#You may obtain a copy of the License at
#
#http://www.apache.org/licenses/LICENSE-2.0
#
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.
#
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