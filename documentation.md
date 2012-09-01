---
layout: layout
---


## Dependencies

This module has no external dependencies, other than a running database of your choise


## Name

The module name is eban-alchemy-persistor-*version*
You have to replace version with the current version of the module, which as the time of writing is `v0.1`



## Configuration

The alchemy-persistor module takes the following configuration:

    {
        "address":"<address to listen to>",
        "protocol":"<sql alchemy protocol>",
        "host":"<database server hostname>",
        "db_name":"<name of database>",
        "username":"<username>",
        "password":"<password>",
        "model-path":"<path to the folder containing model definitions>"
    }

For example:

    {
        "address":"mysql.persistor",
        "protocol":"mysql",
        "host":"myserver.com",
        "db_name":"mydb",
        "username":"myname",
        "password":"mypassword",
        "model-path":"models"
    }

Let's take a look at each field in turn:

* `address` The main address for the module. Every module has a main address. Defaults to `alchemy-persistor`.
* `protocol` Name of the sql alchemy protocol to use to connect to database. Defaults to `sqlite`.
* `host` Host name or ip address of the MongoDB instance. Defaults to `localhost`.
* `db_name` Name of the database to use.
* `username` Username to use to connect to database.
* `password` Password to use to connect to database.
* `model-path` path to the folder containing model definitions. This folder should
    containing python files with definition of your sql alchemy model classes.

## Operations

The module supports the operations above. Please note that the operations protocol
is identical to that used by [mongo persistor module][2].

This allow you to configure the use of alchemy persistor in every place where
you used Mongo persistor, without need to change your code.

In every operation, the `collection` param must refer to the name of
the Sql Alchemy class you want to work with.

### Save

Saves a row in the database.

To save a row send a JSON message to the module main address:

    {
        "action": "save",
        "collection": <collection>,
        "document": <document>
    }

Where:
* `collection` is the name of the Sql Alchemy table that you wish to save the document in. This field is mandatory.
* `document` is the JSON document that you wish to save.

An example would be:

    {
        "action": "save",
        "collection": "users",
        "document": {
            "name": "tim",
            "age": 1000,
            "shoesize": 3.14159,
            "username": "tim",
            "password": "wibble"
        }
    }

When the save complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "document": {
            "name": "tim",
            "age": 1000,
            "shoesize": 3.14159,
            "username": "tim",
            "password": "wibble"
        }
    }

The reply will also contain a field `document` with the row that was saved to database.
This could be useful to retrieve the value of fields eventually setted by the Sql Alchemy class
or by the database server.

If the row already exists in the database it will be updated, otherwise it will be inserted.


If an error occurs in saving the document a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where `message` is an error message.


### Find

Finds matching documents in the database.

To find documents send a JSON message to the module main address:

    {
        "action": "find",
        "collection": <collection>,
        "matcher": <matcher>,
        "limit": <limit>,
        "batch_size": <batch_size>
    }

Where:
* `collection` is the name of the Sql Alchemy table that you wish to search in in. This field is mandatory.
* `matcher` is a JSON object that you want to match against to find matching documents. This obeys the normal mongo persistor matching rues.
    Otherwise, it can be a string representing a query to issue on the db
* `limit` is a number which determines the maximum total number of rows to return. This is optional. By default all rows are returned.

An example would be:

    {
        "action": "find",
        "collection": "orders",
        "matcher": {
            "item": "cheese"
        }
    }

*This would return all orders where the `item` field has the value `cheese`*

or using the query syntax for the matcher:

    {
        "action": "find",
        "collection": "orders",
        "matcher": "item == 'cheese' or total > 100"
        }
    }

*This would return all orders where the `item` field has the value `cheese`, and the `qty` field is greater than 100*



When the find complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "results": <results>
    }

Where `results` is a JSON array containing the results of the find operation. For example:

    {
        "status": "ok",
        "results": [
            {
                "user": "tim",
                "item": "cheese",
                "total": 123.45
            },
            {
                "user": "bob",
                "item": "cheese",
                "total": 12.23
            },
            {
                "user": "jane",
                "item": "cheese",
                "total": 50.05
            }
        ]
    }

If an error occurs in finding the documents a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where `message` is an error message.


### Find One

Finds a single matching document in the database.

To find a document send a JSON message to the module main address:

    {
        "action": "findone",
        "collection": <collection>,
        "matcher": <matcher>
    }

Where:
* `collection` is the name of the Sql Alchemy table that you wish to search in in. This field is mandatory.
* `matcher` is a JSON object that you want to match against to find matching documents. This obeys the normal mongo persistor matching rues.
    Otherwise, it can be a string representing a query to issue on the db

If more than one document matches, just the first one will be returned.

An example would be:

    {
        "action": "findone",
        "collection": "orders",
        "matcher": {
            "item": "cheese"
        }
    }


*This would return the first order where the `item` field has the value `cheese`*

or using the query syntax for the matcher:

    {
        "action": "findone",
        "collection": "orders",
        "matcher": "item == 'cheese' or total > 100"
        }
    }

*This would return the first order where the `item` field has the value `cheese`, and the `qty` field is greater than 100*


When the find complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "result": <result>
    }

If an error occurs in finding the documents a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where `message` is an error message.

### Delete

Deletes matching documents in the database.

To delete documents send a JSON message to the module main address:

    {
        "action": "delete",
        "collection": <collection>,
        "matcher": <matcher>
    }


Where:
* `collection` is the name of the Sql Alchemy table that you wish to delete from. This field is mandatory.
* `matcher` is a JSON object that you want to match against to find matching documents. This obeys the normal mongo persistor matching rues.
    Otherwise, it can be a string representing a query to issue on the db

All documents that match will be deleted.

An example would be:

    {
        "action": "delete",
        "collection": "items",
        "matcher": {
            "item": "cheese"
        }
    }


*This would delete all orders where the `item` field has the value `cheese`*


or using the query syntax for the matcher:

    {
        "action": "delete",
        "collection": "orders",
        "matcher": "item == 'cheese' or total > 100"
        }
    }

*This would delete all orders where the `item` field has the value `cheese`, and the `qty` field is greater than 100*

When the find complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "number": <number>
    }

Where `number` is the number of documents deleted.

If an error occurs in finding the documents a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where `message` is an error message.


[1]: http://www.sqlalchemy.org/
[2]: https://github.com/vert-x/mod-mongo-persistor