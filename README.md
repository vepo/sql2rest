# SQL2Rest

[![Build Status](https://travis-ci.org/vepo/sql2rest.svg?branch=master)](https://travis-ci.org/vepo/sql2rest)
[![Build Status](https://sonarcloud.io/api/project_badges/measure?project=org.vepo%3Asql-to-rest&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.vepo%3Asql-to-rest)

A simple SQL Parser that produces Rest queries using Antlr4

## Examples

| Input                                                                                    | Output                                                                       |
|------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| SELECT * FROM Device                                                                     | /device                                                                      |
| SELECT * FROM Device WHERE id = 2                                                        | /device?search=id:2                                                          |
| SELECT * FROM Device WHERE id=2 OR id = 3                                                | /device?search=id:2 OR id:3                                                  |
| SELECT * FROM Device WHERE name = 'John' AND (id=2 OR id=3)                              | /device?search=name:'John' AND ( id:2 OR id:3 )                              |
| SELECT * FROM Device WHERE name = 'John' AND ((id=2 OR id=3) AND (age > 30 or age < 10)) | /device?search=name:'John' AND ( ( id:2 OR id:3 ) AND ( age>30 OR age<10 ) ) |
