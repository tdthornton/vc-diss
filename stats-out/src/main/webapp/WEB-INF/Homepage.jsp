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
    <br><br><h4>This week's top crunchers are:</h4>
<div id="graphHolder">
    <div id="myfirstchart" style="height: 300px; width:90%" ></div>
</div>
    <br><br><br><br><br>
    Or log in to view and adjust your own crunching:
    <br><br><br>

<div id="signinholder">
<form id="signinform" class="form-inline" id="login" action="/userview" method="post">
    <div id="formelementsholder">
    <div class="form-group">
        <input type="text" class="form-control" name="username" value="admin1">
        <%--<input type="text" class="form-control" name="username" placeholder="Username">--%>
    </div>
    <div class="form-group">
        <input type="password" class="form-control" name="password" value="pass">
        <%--<input type="text" class="form-control" name="password" placeholder="Password">--%>
    </div>


    <button type="submit" class="btn btn-default" value="Submit">Sign In</button>
        <br><br><p class="resetholderz"><a href="${reseturl}">reset password</a></p></div>
</form>
</div>
    <br><br><br><br>
    <span class="myfooter" align="center">about vc-diss - T&C - privacy policy - <a href="${registerurl}">register</a></span>
</div>

<script>
    $(document).ready(function () {


        var statss = [
            <c:forEach items="${stats}" var="stat" varStatus="status">
            {
                user: '${stat.user}',
                credits: '${stat.credits}',
                week: '${stat.week}'
            }
            <c:if test="${!status.last}">
            ,
            </c:if>
            </c:forEach>
        ];


        savedBar = new Morris.Bar({
            // ID of the element in which to draw the chart.
            element: 'myfirstchart',
            // Chart data records -- each entry in this array corresponds to a point on
            // the chart.
            data: statss,
            // The name of the data record attribute that contains x-values.
            xkey: 'user',
            // A list of names of data record attributes that contain y-values.
            ykeys: ['credits'],
            // Labels for the ykeys -- will be displayed when you hover over the
            // chart.
            axes: 'x',
            hideHover: 'always',
            grid: false
        });

    });
</script>

</body>
</html>
