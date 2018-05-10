@echo off

set dict=cmudict-en-us.dict
echo -------DICTIONARY: %dict%-------

XCOPY "%dict%" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
XCOPY "%dict%" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

echo -------HASHING TIME.gram-------
CertUtil -hashfile "%dict%" MD5 | find /i /v "md5" | find /i /v "certutil" > %dict%.md5

XCOPY "%dict%.md5" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
XCOPY "%dict%.md5" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

REM echo -------DATES.gram-------

REM XCOPY "dates.gram" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
REM XCOPY "dates.gram" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

REM echo -------HASHING DATES.gram-------
REM CertUtil -hashfile "dates.gram" MD5 | find /i /v "md5" | find /i /v "certutil" > dates.gram.md5

REM XCOPY "dates.gram.md5" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
REM XCOPY "dates.gram.md5" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

REM echo -------TIME.gram-------

REM XCOPY "time.gram" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
REM XCOPY "time.gram" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

REM echo -------HASHING TIME.gram-------
REM CertUtil -hashfile "time.gram" MD5 | find /i /v "md5" | find /i /v "certutil" > time.gram.md5

REM XCOPY "time.gram.md5" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
REM XCOPY "time.gram.md5" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

REM set lm=en-us.lm.bin
REM set old_lm=lm_csr_5k_nvp_2gram.lm.dmp

REM echo -----deleting old Large Language Model: %old_lm%-----

REM del "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\%old_lm%"
REM del "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\%old_lm%"

REM echo -----deleting old HASH LM------

REM del "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\%old_lm%.md5" 
REM del "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\%old_lm%.md5" 

REM echo -----Large Language Model: %lm%-----

REM XCOPY "%lm%" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
REM XCOPY "%lm%" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

REM echo -----HASHING LM------

 REM CertUtil -hashfile "%lm%" MD5 | find /i /v "md5" | find /i /v "certutil" > %lm%.md5

REM XCOPY "%lm%.md5" "D:\Senior_Project\Sphinx-Assistant\models\src\main\assets\sync\" /Y
REM XCOPY "%lm%.md5" "D:\Senior_Project\Sphinx-Assistant\app\build\intermediates\assets\debug\sync\" /Y

PAUSE