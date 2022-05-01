@echo off
call git checkout smart-box
call:build
COPY D:\workspace\smart-box-vedio\target\wvp-pro.jar wvp-pro.jar
echo '完成wvp-pro打包'

call git checkout wvp-28181-2.0
call:build
COPY D:\workspace\smart-box-vedio\target\wvp-*.jar wvp.jar
echo '完成wvp打包'
call git checkout smart-box
pause
goto:end

:build
cd web_src
call yarn
call yarn build
cd ..
call mvn clean package -Dmaven.test.skip=true
goto:eof
