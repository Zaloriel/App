Для того что бы запустить приложение:
- Скомпилируйте проект: mvn clean package
- mvb spring-boot:run

Swagger UI: http://localhost:8080/
Веб-интерфейс: http://localhost:8080/users
API: http://localhost:8080/api/users

- API Endpoints Пользователи
- POST/api/usersСоздать пользователя
- GET/api/usersПолучить всех пользователей
- GET/api/users/{id}Получить пользователя по ID
- GET/api/users/email/{email}Получить пользователя по email
- PUT/api/users/{id}Обновить пользователя
- DELETE/api/users/{id}Удалить пользователя
