# FE Guideline: Post ho tro User va Mentor

## Co can sua FE khong?

Co. FE can sua cac call API post/like/comment de khong gui actor id/role nua. Backend se lay ca `id` va `role` cua nguoi dang thao tac tu JWT.

Dieu FE can dam bao:

- Moi API create post, like, unlike, check-like, create comment phai gui `Authorization: Bearer <token>`.
- Khong gui `authorId`, `userId`, hoac `role` cho cac action nay nua.
- Khi render response, dung `role` de biet actor la `USER` hay `MENTOR`.

## Role lay tu dau?

Backend doc role tu JWT claim `roles`.

JWT hien dang co dang:

```json
{
  "sub": "12",
  "roles": ["ROLE_MENTOR"]
}
```

Backend se map:

- `ROLE_USER` -> bang `User`
- `ROLE_MENTOR` -> bang `Mentor`

`ADMIN` va `STAFF` chua duoc ho tro dang bai, like hoac comment trong flow nay.

## Create Post

Endpoint:

```http
POST /api/posts
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

Request multipart form-data:

| Field | Type | Required | Ghi chu |
|---|---|---:|---|
| `title` | string | Yes | Tieu de bai viet |
| `content` | string | Yes | Noi dung |
| `summary` | string | No | Tom tat |
| `coverImg` | file | Yes | Anh cover |
| `tags` | array/string tuy FE dang gui | No | Giu format hien tai |
| `status` | string | Yes | `PUBLISHED`, `DRAFT`, `ARCHIVED` |

Khong gui:

```text
authorId
role
```

Backend tu lay author theo JWT.

## Like Post

Endpoint:

```http
POST /api/posts/likes
Content-Type: application/json
Authorization: Bearer <token>
```

Request:

```json
{
  "postId": 1
}
```

Khong gui:

```text
userId
role
```

## Unlike Post

Endpoint:

```http
DELETE /api/posts/likes/{postId}
Authorization: Bearer <token>
```

Vi du:

```http
DELETE /api/posts/likes/1
```

Khong gui `userId` tren path va khong gui `role` query param.

## Check Liked

Endpoint:

```http
GET /api/posts/likes/{postId}/check
Authorization: Bearer <token>
```

Vi du:

```http
GET /api/posts/likes/1/check
```

Response:

```json
{
  "isLiked": "true"
}
```

## Create Comment

Endpoint:

```http
POST /api/posts/comments
Content-Type: application/json
Authorization: Bearer <token>
```

Request comment goc:

```json
{
  "postId": 1,
  "content": "Bai viet rat huu ich",
  "parentCommentId": null
}
```

Request reply:

```json
{
  "postId": 1,
  "content": "Minh dong y voi y nay",
  "parentCommentId": 5
}
```

Khong gui:

```text
userId
role
```

## Response can doc them gi?

Post author co them `id` va `role`:

```json
{
  "author": {
    "id": 12,
    "role": "MENTOR",
    "name": "Mentor Name",
    "avatar": "https://..."
  }
}
```

Like response co them `userId` va `role`:

```json
{
  "userId": 12,
  "role": "MENTOR",
  "userName": "Mentor Name",
  "userAvatar": "https://..."
}
```

Comment response co them `userId` va `role`:

```json
{
  "id": 10,
  "userId": 12,
  "role": "MENTOR",
  "userName": "Mentor Name",
  "userAvatar": "https://...",
  "content": "Bai viet rat huu ich",
  "parentCommentId": 0,
  "createdAt": "2026-08-15T..."
}
```

## FE can sua nhung cho nao?

- Bo `authorId` va `role` khoi request create post.
- Bo `userId` va `role` khoi request like/comment.
- Doi unlike tu `/api/posts/likes/{postId}/{userId}?role=...` thanh `/api/posts/likes/{postId}`.
- Doi check-like tu `/api/posts/likes/{postId}/check/{userId}?role=...` thanh `/api/posts/likes/{postId}/check`.
- Dam bao cac request tren luon co `Authorization: Bearer <token>`.
- Khi render author/like/comment, dung `role` trong response neu can hien thi badge hoac dieu huong profile user/mentor.

## Luu y cho BE/DB

Neu gap loi PostgreSQL dang nay:

```text
null value in column "user_id" of relation "postlike" violates not-null constraint
```

thi DB dang con schema cu, can alter de `user_id` nullable va them `mentor_id`.

SQL tham khao:

```sql
ALTER TABLE postlike ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE postcomment ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE post ADD COLUMN IF NOT EXISTS mentor_id integer;
ALTER TABLE postlike ADD COLUMN IF NOT EXISTS mentor_id integer;
ALTER TABLE postcomment ADD COLUMN IF NOT EXISTS mentor_id integer;

ALTER TABLE post
    ADD CONSTRAINT fk_post_mentor
    FOREIGN KEY (mentor_id) REFERENCES mentor(id);

ALTER TABLE postlike
    ADD CONSTRAINT fk_postlike_mentor
    FOREIGN KEY (mentor_id) REFERENCES mentor(id);

ALTER TABLE postcomment
    ADD CONSTRAINT fk_postcomment_mentor
    FOREIGN KEY (mentor_id) REFERENCES mentor(id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_postlike_post_user
    ON postlike (post_id, user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_postlike_post_mentor
    ON postlike (post_id, mentor_id)
    WHERE mentor_id IS NOT NULL;
```

Sau khi deploy code moi, can restart backend de service dung JWT actor moi.
