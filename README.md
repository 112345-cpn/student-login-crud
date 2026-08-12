# Student Recruitment Backend

## Business rules

- Students do not log in and do not have passwords.
- Only administrators log in, using the administrator name and password.
- Student records contain only student number, name, score, and registration time.
- A student number must be exactly 10 digits and start with `2600`.

## API

### Administrator

- `POST /api/admin/login`: log in and create the administrator session.
- `POST /api/admin/logout`: invalidate the administrator session.

Request body:

```json
{
  "username": "Administrator name",
  "password": "Administrator password"
}
```

### Student records

All `/api/students/**` endpoints require an active administrator session.

- `POST /api/students`: create a record. `studentNo` and `name` are required; `score` is optional; `registerTime` defaults to the current time.
- `PUT /api/students/{studentNo}`: update the record identified by student number.
- `GET /api/students/check?studentNo=2600123456`: validate format and uniqueness.
- `GET /api/students/{studentNo}`: get a record by student number.
- `DELETE /api/students/{studentNo}`: delete a record by student number.

## Database

Run `src/main/resources/schema.sql` to create the `admin` and `student` tables. BCrypt hashes are recommended for administrator passwords. The login service still accepts existing plaintext passwords so current data can be migrated gradually.
