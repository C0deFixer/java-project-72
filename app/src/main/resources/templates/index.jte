@import hexlet.code.dto.MainPage
@import hexlet.code.util.NamedRoutes
@param MainPage page

@template.layout.page(
page = page,
content = @`
<%--    @if(!page.isVisited())
        This message is showed just once. To see it again, remove cookies
    @endif
    @if(page.getCurrentUser() != null)
        Welcome, ${page.getCurrentUser()}. if you want to sign out remove cookie called JSESSIONID
    @endif--%>

    <main class="flex-grow-1">

        <section>

            <div class="container-fluid bg-dark p-5">
                <div class="row">
                    <div class="col-md-10 col-lg-8 mx-auto text-white">
                        <h1 class="display-3 mb-0">Анализатор страниц</h1>
                        <p class="lead">Бесплатно проверяйте сайты на SEO пригодность</p>
                        <form action="${NamedRoutes.urls()}" method="post" class="rss-form text-body">
                            <div class="row">
                                <div class="col">
                                    <div class="form-floating">
                                        <input id="url-input" autofocus="" type="text" required="" name="url"
                                               aria-label="url" class="form-control w-100" placeholder="ссылка"
                                               autocomplete="off">
                                        <label for="url-input">Ссылка</label>
                                    </div>
                                </div>
                                <div class="col-auto">
                                    <button type="submit" class="h-100 btn btn-lg btn-primary px-sm-5">Проверить
                                    </button>
                                </div>
                            </div>
                        </form>
                        <p class="mt-2 mb-0 text-muted">Пример: https://www.example.com</p>
                    </div>
                </div>
            </div>

        </section>
    </main>
`
)