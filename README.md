# SEARCH ENGINE
### Поисковой движок - приложение, которое позволяет индексировать страницы и осуществлять по ним быстрый поиск.
## Используемые технологии
- __Spring Framework__
- __JPA__
- __JSOUP__
- __SQL__
- __Morphology Library Lucene__
## Функционал приложения
+ __Отоброжение общей статистики__
- __Запуск и остановка индексации__
- __Поиск слов по сайтам__
## __Dashboard__. Эта вкладка открывается по умолчанию. На ней отображается общая статистика по всем сайтам, а также детальная статистика и статус по каждому из сайтов (статистика, получаемая по запросу /api/statistics).
![dashboard](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/Dashboard.PNG)
## __Management__. На этой вкладке находятся инструменты управления поисковым движком — запуск и остановка полной индексации (переиндексации), а также возможность добавить (обновить) отдельную страницу по ссылке
![managment](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/Managment.PNG)
## __Search__. Эта страница предназначена для тестирования поискового движка. На ней находится поле поиска, выпадающий список с выбором сайта для поиска, а при нажатии на кнопку «Найти» выводятся результаты поиска (по API-запросу /api/search)
![search](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/Search.PNG)

## Индиксация сайтов
1. __в адресную строку сайтов введите http://localhost:8080/__
![search1](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/%D0%BF%D0%BE%D0%B8%D1%81%D0%BA%D0%BE%D0%B2%D0%B0%D1%8F%20%D1%81%D1%82%D1%80%D0%BE%D0%BA%D0%B0.PNG)
2. __Выберете вкладку MANAGEMENT__
![man1](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/man1.png)
3. __Нажмите на кнопку START INDEXING__
![man2](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/man2.png)
## Поиск слов по сайту
1. __Выберете вкладку SEARCH__
![sear1](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/Search1.PNG)
2. __В выпадающем списке выберете сайт, по которому нужно выполнить поиск, введите слово, и нажмите кнопку SEARCH__
![sear2](https://raw.githubusercontent.com/Unpier/Search_Engine/main/img/Search2.PNG)

