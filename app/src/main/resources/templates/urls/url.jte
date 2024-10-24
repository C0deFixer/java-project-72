@import hexlet.code.dto.MainPage
@import hexlet.code.dto.UrlPage
@import hexlet.code.util.NamedRoutes
@param UrlPage page
!{var flashTipe = page.getFlashTipe().isBlank() ? "alert alert-success" : page.getFlashTipe();}

@template.layout.page(
page = page,
content = @`
    <main class="flex-grow-1">

        <section>

            <div class="container-lg mt-5">
                <h1>Сайт: ${page.getName()}</h1>

                <table class="table table-bordered table-hover mt-3">
                    <tbody>
                    <tr>
                        <td>ID</td>
                        <td>${page.getId()}</td>
                    </tr>
                    <tr>
                        <td>Имя</td>
                        <td>${page.getName()}</td>
                    </tr>
                    <tr>
                        <td>Дата создания</td>
                        <td>${page.getDateAsString(page.getCreatedAt())}</td>
                    </tr>
                    </tbody>
                </table>

                <h2 class="mt-5">Проверки</h2>
                <form method="post" action="/urls/1/checks">
                    <button type="submit" class="btn btn-primary">Запустить проверку</button>
                </form>

                <table class="table table-bordered table-hover mt-3">
                    <thead>
                    <tr>
                        <th class="col-1">ID</th>
                        <th class="col-1">Код ответа</th>
                        <th>title</th>
                        <th>h1</th>
                        <th>description</th>
                        <th class="col-2">Дата проверки</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>

        </section>
    </main>
`
)