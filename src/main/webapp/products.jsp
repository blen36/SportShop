<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<html>
<head>

    <meta charset="UTF-8">

    <title>Каталог товаров</title>

    <link rel="stylesheet" href="styles.css">

</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Каталог товаров</h1>

    <c:if test="${param.error == 'stock'}">

        <div class="error-message">
            Недостаточно товара на складе или достигнут лимит
        </div>

    </c:if>

    <form method="get"
          action="products"
          class="filters">

        <div class="filter-row">

            <input type="text"
                   name="q"
                   placeholder="Поиск по названию, описанию или характеристикам"
                   value="${query}"/>

            <input type="text"
                   name="brand"
                   placeholder="Бренд"
                   value="${brand}"/>

            <input type="number"
                   name="minPrice"
                   placeholder="Цена от"
                   value="${minPrice}"
                   step="0.01"/>

            <input type="number"
                   name="maxPrice"
                   placeholder="Цена до"
                   value="${maxPrice}"
                   step="0.01"/>

        </div>

        <div class="filter-row">

            <select name="categoryId">

                <option value="">
                    Все категории
                </option>

                <c:forEach var="c"
                           items="${categories}">

                    <option value="${c.id}"
                        <c:if test="${c.id == categoryId}">
                            selected
                        </c:if>>

                        ${c.name}

                    </option>

                </c:forEach>

            </select>

            <select name="minRating">

                <option value="">
                    Любой рейтинг
                </option>

                <option value="4"
                    <c:if test="${minRating == '4'}">
                        selected
                    </c:if>>
                    От 4 ★
                </option>

                <option value="3"
                    <c:if test="${minRating == '3'}">
                        selected
                    </c:if>>
                    От 3 ★
                </option>

                <option value="2"
                    <c:if test="${minRating == '2'}">
                        selected
                    </c:if>>
                    От 2 ★
                </option>

            </select>

            <select name="sort">

                <option value="">
                    Сортировка
                </option>

                <option value="price_asc"
                    <c:if test="${sort == 'price_asc'}">
                        selected
                    </c:if>>
                    Цена ↑
                </option>

                <option value="price_desc"
                    <c:if test="${sort == 'price_desc'}">
                        selected
                    </c:if>>
                    Цена ↓
                </option>

                <option value="name"
                    <c:if test="${sort == 'name'}">
                        selected
                    </c:if>>
                    Название
                </option>

                <option value="rating_desc"
                    <c:if test="${sort == 'rating_desc'}">
                        selected
                    </c:if>>
                    Рейтинг
                </option>

                <option value="stock_desc"
                    <c:if test="${sort == 'stock_desc'}">
                        selected
                    </c:if>>
                    Наличие
                </option>

            </select>

            <button type="submit">
                Найти 🔍
            </button>

            <a href="products"
               class="reset-link">
                Сбросить
            </a>

        </div>

    </form>

    <p class="muted">
        Найдено товаров: ${totalProducts}
    </p>

    <c:choose>

        <c:when test="${empty products}">

            <div class="empty-message">
                Ничего не найдено
            </div>

        </c:when>

        <c:otherwise>

            <div class="products">

                <c:forEach var="p"
                           items="${products}">

                    <div class="product-card">

                        <a href="product?id=${p.id}"
                           class="image-link">

                            <c:choose>

                                <c:when test="${not empty p.imageUrl}">

                                    <img src="${p.imageUrl}"
                                         alt="${p.name}"
                                         class="product-image"/>

                                </c:when>

                                <c:otherwise>

                                    <div class="product-placeholder">
                                        Нет изображения
                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </a>

                        <h2>
                            <a href="product?id=${p.id}"
                               class="plain-link">
                                <c:out value="${p.name}"/>
                            </a>
                        </h2>

                        <p class="product-description">
                            <c:out value="${p.description}"/>
                        </p>

                        <p>
                            <b>Бренд:</b>

                            <c:choose>

                                <c:when test="${not empty p.brand}">
                                    <c:out value="${p.brand}"/>
                                </c:when>

                                <c:otherwise>
                                    -
                                </c:otherwise>

                            </c:choose>

                        </p>

                        <p class="rating">
                            ★

                            <c:choose>

                                <c:when test="${p.reviewsCount > 0}">
                                    <fmt:formatNumber value="${p.averageRating}"
                                                      maxFractionDigits="1"/>
                                    <span class="muted">
                                        (${p.reviewsCount})
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    <span class="muted">
                                        Нет отзывов
                                    </span>
                                </c:otherwise>

                            </c:choose>

                        </p>

                        <c:if test="${not empty p.attributes && p.attributes != '{}'}">

                            <div class="attributes-box">
                                <b>Характеристики:</b>
                                <pre><c:out value="${p.attributes}"/></pre>
                            </div>

                        </c:if>

                        <p class="price">
                            ${p.price} ₽
                        </p>

                        <p>
                            <b>В наличии:</b>
                            ${p.stock}
                        </p>

                        <c:if test="${p.stock > 0 && p.stock <= 5}">

                            <p class="warning-text">
                                Осталось мало товара ⚠
                            </p>

                        </c:if>

                        <div class="card-actions">

                            <a href="product?id=${p.id}"
                               class="button-link secondary-link">
                                Подробнее
                            </a>

                            <form method="post"
                                  action="cart"
                                  class="inline-form">

                                <input type="hidden"
                                       name="productId"
                                       value="${p.id}"/>

                                <input type="hidden"
                                       name="action"
                                       value="add"/>

                                <c:choose>

                                    <c:when test="${p.stock <= 0}">

                                        <button disabled
                                                class="disabled-button">

                                            Нет в наличии

                                        </button>

                                    </c:when>

                                    <c:otherwise>

                                        <button>
                                            В корзину
                                        </button>

                                    </c:otherwise>

                                </c:choose>

                            </form>

                            <form method="post"
                                  action="favorite"
                                  class="inline-form">

                                <input type="hidden"
                                       name="productId"
                                       value="${p.id}"/>

                                <input type="hidden"
                                       name="action"
                                       value="add"/>

                                <button type="submit"
                                        class="secondary-button">
                                    ❤️
                                </button>

                            </form>

                            <form method="post"
                                  action="compare"
                                  class="inline-form">

                                <input type="hidden"
                                       name="productId"
                                       value="${p.id}"/>

                                <input type="hidden"
                                       name="action"
                                       value="add"/>

                                <button type="submit"
                                        class="secondary-button">
                                    Сравнить
                                </button>

                            </form>

                        </div>

                    </div>

                </c:forEach>

            </div>

        </c:otherwise>

    </c:choose>

    <div class="pagination">

        <c:if test="${page > 1}">

            <c:url var="prevUrl"
                   value="products">

                <c:param name="page"
                         value="${page - 1}"/>

                <c:param name="q"
                         value="${query}"/>

                <c:param name="brand"
                         value="${brand}"/>

                <c:param name="minPrice"
                         value="${minPrice}"/>

                <c:param name="maxPrice"
                         value="${maxPrice}"/>

                <c:param name="minRating"
                         value="${minRating}"/>

                <c:param name="categoryId"
                         value="${categoryId}"/>

                <c:param name="sort"
                         value="${sort}"/>

            </c:url>

            <a href="${prevUrl}">
                ⬅ Назад
            </a>

        </c:if>

        <span>
            Страница ${page} из ${totalPages}
        </span>

        <c:if test="${page < totalPages}">

            <c:url var="nextUrl"
                   value="products">

                <c:param name="page"
                         value="${page + 1}"/>

                <c:param name="q"
                         value="${query}"/>

                <c:param name="brand"
                         value="${brand}"/>

                <c:param name="minPrice"
                         value="${minPrice}"/>

                <c:param name="maxPrice"
                         value="${maxPrice}"/>

                <c:param name="minRating"
                         value="${minRating}"/>

                <c:param name="categoryId"
                         value="${categoryId}"/>

                <c:param name="sort"
                         value="${sort}"/>

            </c:url>

            <a href="${nextUrl}">
                Вперёд ➡
            </a>

        </c:if>

    </div>

</div>

</body>
</html>