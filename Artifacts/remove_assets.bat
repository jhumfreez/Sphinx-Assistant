@echo off

set list=()

for %%i in %list% do (
	del "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\%%i"
	del "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\%%i"
)

rem del "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\"
rem del "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\"
echo -----ASSETS DELETED-----
PAUSE