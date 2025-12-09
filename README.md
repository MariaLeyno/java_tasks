# Console Marketplace
Java project for Y_LAB Java course.

Marketplace Application allows to manage items in shop catalog.

There are 2 predefined users:
1. 'Root' with password 'toortoor' and full access.
2. 'User' with password 'resu' and read access.

Users may add a new account and sign in into the system. All new accounts will have full access to the items catalog, but no access to system inner information.

In shop catalog users may:
1. Filter existing items by one or more fields.
2. Add new items.
3. Update existing items.
4. Delete existing items.

Application functionality is available through HTTP endpoints:
- Endpoints for user accounts management:
  - url: /Marketplace/user
    - POST method is used to create a new user account: 'login', 'password' and 'password_again' query parameters are required.
    - PUT method is used to sign in: 'login' and 'password' query parameters are required. The endpoint returns authorization token in ''Auth token'' header.
  - url: /Marketplace/catalog, all requests require authorization token sent at the Authorization header (for users with READ access only GET endpoint is available)
    - GET method is used to get catalog items: 'name', 'category', 'brand' and 'price' query parameters may be specified for filtering.
    - POST method is used to create a new catalog item: 'name', 'category' and 'brand' fields should be specified in the body, 'price' field may be null.
    - PUT method is used to update existing catalog items: filtering parameters may be specified as query parameters, fields values to update - in the body.
    - DELETE method is used to delete existing catalog items: filtering parameters may be specified as query parameters.

All entity objects are stored at PostreSQL database. Table indexes provide fast access to entities. Liquibase manages data migration and table structure at the database.

All REST requests are logged to a file and stored as events with parameters to the PostgreSQL database for auditing.