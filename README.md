Для того что бы запустить приложение:
- Скомпилируйте проект: mvn clean package
- mvb spring-boot:run

Приложение будет доступно по адресу: http://localhost:8080

API Endpoints Пользователи
POST/api/usersСоздать пользователя
GET/api/usersПолучить всех пользователей
GET/api/users/{id}Получить пользователя по ID
GET/api/users/email/{email}Получить пользователя по email
PUT/api/users/{id}Обновить пользователя
DELETE/api/users/{id}Удалить пользователя
