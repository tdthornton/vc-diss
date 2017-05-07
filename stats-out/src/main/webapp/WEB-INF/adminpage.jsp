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

    <link type="text/css" rel="stylesheet" href="/css/main.css?v=1"/>
</head>

<body>
<div align="center" class="container">
    <h1><a href="${footerUrls[3]}">vc-diss</a></h1>

    <br><br><br>
        <h3>Welcome to the admin page for ${app}</h3><br>
        <h5>You can update the code for this app below.</h5><br><br><br>


    <label class="btn btn-default btn-file">
    <form class="form-inline" action="${updateCodeUrl}" enctype="multipart/form-data" method="POST">

        <input class="btn btn-default btn-file" type="file" name="newcodefile" hidden><br><br>

        <input type="text" class="form-control" name="md5" placeholder="md5 hash">
        <input type="text" name="token" value="${accessToken}" hidden>
        <button type="submit" class="btn btn-default">Submit</button>
    </form>
    </label>

    <br><br>

    <label class="btn btn-default btn-file">
        <form class="form-inline" action="${uploadInputsUrl}" enctype="multipart/form-data" method="POST">

            <input class="btn btn-default btn-file" type="file" name="newInputsFile" hidden><br><br>

            <input type="text" class="form-control" name="md5" placeholder="md5 hash">
            <input type="text" name="token" value="${accessToken}" hidden>
            <button type="submit" class="btn btn-default">Submit</button>
        </form>
    </label>

<br><br><br><br><br><br><br>
    <div class="myfooter" align="center"><a href="${footerUrls[0]}">about vc-diss</a> - <a href="${footerUrls[1]}">T&C</a> - <a href="${footerUrls[2]}">privacy policy</a> - <a href="/">log out</a></div>
</div>


</body>
</html>
