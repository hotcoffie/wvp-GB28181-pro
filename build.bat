call git checkout smart-box
call:build
COPY D:\workspace\smart-box-vedio\target\wvp-pro.jar wvp-pro.jar
call git checkout wvp-28181-2.0
call:build
COPY D:\workspace\smart-box-vedio\target\wvp-*.jar wvp.jar
pause
goto:end

:build
cd web_src
call yarn
call yarn build
cd ..
call mvn clean package -Dmaven.test.skip=true
goto:eof
