<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
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
    <h1>vc-diss</h1>
    <br><br><br><h4>Register for a new account:</h4>

    <br><br>

    <div id="registerholder">
        <form id="registerform" class="form-inline" id="register" action="/register" method="post">
            <div id="formelementsholder">
                <div class="form-group paddown">
                    <input id="usernamefield" type="text" class="form-control paddown" name="username" value="test188">
                    <%--<input type="text" class="form-control" name="username" placeholder="Username">--%>
                </div>
                <br>
                <div class="form-group paddown">
                    <input id="passwordfield" type="password" class="form-control" name="password" value="longenoughpass11">
                    <%--<input type="text" class="form-control" name="password" placeholder="Password">--%>
                </div>
                <br>
                <div class="form-group paddown">
                    <input id="emailholder" type="text" class="form-control floatright" name="email" value="test18@emailserver.com">
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
    <span class="myfooter" align="center">about vc-diss - T&C - privacy policy - <a href="${registerurl}">register</a></span>
</div>
<script>

</script>



</body>
</html>