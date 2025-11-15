# Console Marketplace
Java project for Y_LAB Java course.

Console Marketplace application allows to manage items in shop catalog.

There are 2 predefined users:
1. 'Root' with password 'toortoor' and full access.
2. 'User' with password 'resuresu' and read access.

Users may add a new account and sign in into the system. All new accounts will have full access to the items catalog, but no access to system inner information.

In shop catalog users may:
1. Filter existing items by one or more fields.
2. Add new items.
3. Update existing items.
4. Delete existing items.

To speed up filtration the items catalog has in-memory indexes. The last requests are stored at the in-memory cache.

Between the application runs items catalog and accounts data are stored into files.

Users may close the application just to type 'exit' at the console.

Known issues:
1. User authentication is supported, developing of user authorization is in progress.
2. User passwords should be hashed for storing and hidden for printing.
3. Jar-application doesn't support data storing between the application runs.
4. The system just has expansion points for showing audit and other system information. 