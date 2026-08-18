# Lime Core

Базовый CoreMod для проектов Lime на Minecraft Forge 1.12.2.

## Требования

- JDK 8
- Minecraft 1.12.2
- Minecraft Forge 14.23.5.2847 или новее для 1.12.2

## Сборка

```powershell
.\gradlew.bat build
```

Готовый JAR создаётся в `build/libs`.

## Возможности

- Сохраняет выбранный нечётный масштаб интерфейса при использовании
  Unicode-шрифта. Например, масштаб 3 больше не уменьшается до 2 при выборе
  русского языка.
- Загружает настройки CoreMod до инициализации обычных Forge-модов.
- Безопасно отказывается от ASM-патча, если структура целевого класса не
  соответствует ожидаемой Minecraft 1.12.2.
- Выводит итоговое состояние bytecode-патчей в журнал запуска.

## Конфигурация

После первого запуска создаётся `config/limecore.properties`:

```properties
patches.unicodeGuiScale=true
diagnostics.logging=true
```

- `patches.unicodeGuiScale` включает сохранение нечётного масштаба интерфейса.
- `diagnostics.logging` включает сводку о состоянии патчей в журнале.

Настройки читаются ранним загрузчиком CoreMod, поэтому применяются уже при
следующем запуске Minecraft.

## Разработка

Для запуска клиента нужен JDK 8:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-8.0.432.6-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient
```

Проверка и сборка:

```powershell
.\gradlew.bat clean build
```

## Изменения 0.2.0

- Добавлена ранняя конфигурация CoreMod.
- Unicode GUI Scale patch можно отключить.
- Сопоставление bytecode теперь проверяет класс, owner, opcode, сигнатуру и
  точное количество найденных вызовов.
- Исправлено obfuscated-имя `ScaledResolution`: `bit` вместо `bip`.
- Добавлены диагностика патчей и отрицательные ASM-тесты.
- Добавлена автоматическая сборка на JDK 8.

## Автор

DemonicRous
