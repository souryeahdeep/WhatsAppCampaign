## Installation Guidance

1. To run frontend :
     1. cd csv_generator/
     2. npm run dev - This runs the Frontend App.
2. To run the backend from Parent Folder :
    mvn spring-boot:run

Frontend Runs at localhost:5173 and Backend Runs at localhost:8080 (default)

If your default ports are busy on other work, change the backend port from application.yml file in src/main/resouces.
You will find a db.sql file in the parent folder it contains dummy datas. Be sure to insert the data in the PostgreSQL DB, so that data can be fetched by the backend server.
