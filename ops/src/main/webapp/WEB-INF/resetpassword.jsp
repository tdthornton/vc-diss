<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
          integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
            integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
            crossorigin="anonymous"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/raphael/2.1.0/raphael-min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.min.js"></script>
    <link type="text/css" rel="stylesheet" href="/css/main.css?v=1"/>
</head>

<body>
<div align="center" class="container">
    <h1>vc-diss</h1>
    <br><br><h4>Enter your email to receive a password reset code:</h4>

    <br><br><br><br><br>

    <div id="resetpasswordholder">
        <form id="resetpasswordform" class="form-inline" id="resetpassword" action="/resetpassword" method="post">
            <div id="formelementsholder">
                <div class="form-group">
                    <input type="text" class="form-control" name="email" value="${prefill_email}">
                    <%--<input type="text" class="form-control" name="username" placeholder="Username">--%>
                </div>

                <button type="submit" class="btn btn-default" value="Submit">Reset</button></div>
        </form>
    </div>
    <br><br><br><br>
    <span class="myfooter" align="center">about vc-diss - T&C - privacy policy - <a href="${registerurl}">register</a></span>
</div>


</body>
</html>
