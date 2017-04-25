<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>

    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <link type="text/css" rel="stylesheet" href="/css/main.css"/>
</head>

<body><br><br><br><br>
<div class="container">
<div class="jumbotron">
    <h1>Welcome, ${username}!</h1>
    <p>You've had ${vmcount} unique crunchers running ${app} in the last hour.</p>

</div>
    <div id="restholder">

        <div class="row">
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="col-md-10" align="center">This week:</div>
                <div class="col-md-1"></div>
            </div></div>
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="col-md-10"></div>
                <div class="col-md-1"></div>
            </div></div>
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="col-md-10" align="center">All time:</div>
                <div class="col-md-1"></div>
            </div></div>
        </div>

        <div class="row">
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="pinsk col-md-10" align="center"><a class="nohover totalcounter">${weeklytotal}</a><br><a class="nohover scorestitle">credits earned</a></div>
                <div class="col-md-1"></div>
            </div></div>
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="col-md-10"><button id="clickMe" class="btn-default img-rounded btn-block playpausebutton" onclick="doFunction();"></button></div>
                <div class="col-md-1"></div>
            </div></div>
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="pinsk col-md-10" align="center"><a class="nohover totalcounter">${alltimetotal}</a><br><a class="nohover scorestitle">credits earned</a></div>
                <div class="col-md-1"></div>
            </div></div>
        </div>
        <div class="row">
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="pinsk col-md-10" align="center"><a class="nohover totalcounter">${badgelevelthisweek}</a><br><a class="nohover scorestitle" href="/badges/SPACE.JPG" download>download badge</a></div>
                <div class="col-md-1"></div>
            </div></div>
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="col-md-10"></div>
                <div class="col-md-1"></div>
            </div></div>
            <div class="col-md-4"><div class="row">
                <div class="col-md-1"></div>
                <div class="pinsk col-md-10" align="center"><a class="nohover totalcounter">${badgelevelalltime}</a><br><a class="nohover scorestitle" href="/badges/SPACE.JPG" download>download badge</a></div>
                <div class="col-md-1"></div>
            </div></div>
        </div>


    </div>
    <br><br><br><br><br>
    <div class="myfooter" align="center">about vc-diss - T&C - privacy policy - <a href="#" onclick="deleteAccount()">delete account</a> - <a href="/">log out</a></div>
</div>
<script type="text/javascript">
    var elem = document.getElementById("clickMe");
    var status = "${status}"
    elem.innerHTML = getGlyph(status);

    $(document).ready(function(){

    });

    function deleteAccount() {
        token = "${accesstoken}";
        username = "${username}"
        deleteAccountUrl = "${deleteAccountUrl}"

        sure = confirm("Are you sure you want to delete the account " + username + "?");

        if (sure) {

            $.ajax({
                url: deleteAccountUrl,
                headers: {'X-Auth-Token': token},
                async: false,
                method: 'GET'
            }).then(function(data, status, xhr) {
                alert('Your account has been deleted');
                document.location.href = "/";
            });

        }


    }

    function getGlyph(data) {
        if (data=="Started" || data=="start") {
            return "<span class=\"glyphicon glyphicon-pause\" aria-hidden=\"true\"></span>";
        } else {
            return "<span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span>";
        }
    }
    function doFunction() {
        ursl = "${togglestatusurl}";
        token = "${accesstoken}";

        $.ajax({
            url: ursl,
            headers: {'X-Auth-Token': token},
            async: false,
            method: 'GET'
        }).then(function(data, status, xhr) {
            var elem = document.getElementById("clickMe");
            elem.innerHTML = getGlyph(data);
        });


    }

</script>

</body>
</html>
