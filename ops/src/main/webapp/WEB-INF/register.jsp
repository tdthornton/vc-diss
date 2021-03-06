<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang ="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>vc-diss</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
          integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
            integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
            crossorigin="anonymous"></script>
    <link type="text/css" rel="stylesheet" href="/css/main.css?v=1"/>
</head>

<body>

<div align="center" class="container">
    <h1><a href="${footerUrls[3]}">vc-diss</a></h1>
    <br><br><br><h4>Register for a new account:</h4>

    <br><br>

    <div id="registerholder">
        <form id="registerform" class="form-inline" id="register" action="/register" method="post">
            <div id="formelementsholder">
                <div class="form-group paddown">
                    <input id="usernamefield" type="text" class="form-control paddown" name="username" placeholder="username">
                </div>
                <br>
                <div class="form-group paddown">
                    <input id="passwordfield" type="password" class="form-control" name="password" placeholder="password">
                </div>
                <br>
                <div class="form-group paddown">
                    <input id="emailholder" type="text" class="form-control floatright" name="email" placeholder="email">
                </div>
                <br>
                <div class="form-group paddown">
                    <label class="checkbox-inline floatleft"><input type="checkbox" name="business" value="true">Business account</label>
                </div>
                <br><br>
                <div class="form-group paddown">
                    <label class="checkbox-inline floatleft"><input type="checkbox" name="agree" value="true">I agree to the T&C and Privacy Policy</label>
                </div>
                <br>
                <button type="submit" class="btn btn-default" value="Submit">Register</button></div>
        </form>
    </div>
    <br><br><br><br>
    <span class="myfooter" align="center"><a href="${footerUrls[0]}">about vc-diss</a> - <a href="${footerUrls[1]}">T&C</a> - <a href="${footerUrls[2]}">privacy policy</a> - <a href="${registerurl}">register</a></span>
</div>
<script>

</script>



</body>
</html>
