@import hexlet.code.util.NamedRoutes
@import hexlet.code.dto.BasePage
@import gg.jte.Content
@param Content content
@param BasePage page = null
!{var flashTipe = page == null || page.getFlashTipe() == null || page.getFlashTipe().isEmpty() ? "alert-success" : page.getFlashTipe();}

<!doctype html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We"
          crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4"
            crossorigin="anonymous"></script>
    <title>Анализатор страниц</title>
</head>

<body>


<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="${NamedRoutes.rootPath()}">Анализатор страниц</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div class="navbar-nav">
                <a class="nav-link" href="${NamedRoutes.rootPath()}">Главная</a>
                <a class="nav-link" href="${NamedRoutes.urls()}">Сайты</a>
            </div>
        </div>
    </div>
</nav>

<%--<p>
    <a href="${NamedRoutes.buildSessionPath()}">Sign in</a>

                </p>--%>

@if(page != null && page.getFlash() != null)
    <div class="rounded-0 m-0 alert alert-dismissible fade show ${flashTipe}" role="alert">
        <p class="m-0">${page.getFlash()}</p>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
@endif
${content}

<footer class="footer border-top py-3 mt-5 bg-light">
    <div class="container-xl">
        <div class="text-center">
            Created by
            <a href="https://github.com/C0deFixer" target="_blank">Dmitry Sharypov</a>
        </div>
    </div>
</footer>
</body>
</html>