# Sample-Project

Простой проект для работы с товарами
1. Экран со списком продуктов (добавление, редактирование, удаление элемента + возможность импорта и экспорта в json файл)
2. Экран создания / редактирования продукта

https://github.com/sashatinkoff/Sample-Project/releases/tag/release/1.0.0
Собранный проект для установки на устройстве

## Нюансы
- В проекте используются два способа открытия интента - старый способ через startActivityForResult и с помощью ActivityResultContracts (идет прямиком из androidx.activity и androidx.fragment)
- Реализован только пикер изображений из галереи, но при этом учитывается, что на некоторых устройствах фотографии делаются только в landscape orientation (актуально для самсунгов, на некоторых пикселях на фронтальной камере)
- Экспорт данных по умолчанию идет в data.json, но можно указать и свой вариант
- При импорте не учитывается mime-type файлов, так что можно попробовать забрать не только text/plain файлы
- Пример файла экспорта https://github.com/sashatinkoff/Sample-Project/blob/master/good.json

## Зависимости и особенности
- Используется паттерн проектирования MVVM
- Немного видоизмененная Clean Architecture
- Используется DI для создания обработчиков productUseCase и pictureUseCase, а также для создания глобального объекта Gson
- Сборка производится в Android Studio 4.2 Canary 4, большинство зависимостей в состоянии alpha (гугловские)
- Для обработка и показа фотографий используется библиотека Glide
- ViewModel в Activity / Fragment создается с помощью ViewModelFactory и схемы [@Binds @IntoMap @ViewModelKey](https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848)
- Данные хранятся в БД Realm
- Все операции IO производятся с помощью корутин в диспетчере Dispatcher.IO

# Что не работает
- Не реализован доступ к файлам на Android 10+ (изменилась схема доступа к файловой системе) 
