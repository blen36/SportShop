<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<html>
<head>

    <meta charset="UTF-8">

    <title>Сравнение товаров</title>

    <link rel="stylesheet" href="styles.css">

</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Сравнение товаров</h1>

    <c:if test="${not empty compareMessage}">

        <div class="error-message">
            ${compareMessage}
        </div>

    </c:if>

    <c:choose>

        <c:when test="${comparisonSaved}">

            <p class="muted">
                Ваш список сравнения сохраняется в аккаунте.
            </p>

        </c:when>

        <c:otherwise>

            <p class="muted">
                Для гостей список сравнения хранится только в текущей сессии.
                Войдите в аккаунт, чтобы сохранить его.
            </p>

        </c:otherwise>

    </c:choose>

    <c:choose>

        <c:when test="${empty compareProducts}">

            <div class="empty-message">

                Список сравнения пуст.

                <br/><br/>

                <a href="products"
                   class="button-link">
                    Перейти в каталог
                </a>

            </div>

        </c:when>

        <c:otherwise>

            <form method="post"
                  action="compare">

                <input type="hidden"
                       name="action"
                       value="clear"/>

                <button class="danger-button">
                    Очистить сравнение
                </button>

            </form>

            <br/>

            <table class="compare-table">

                <tr>
                    <th>Параметр</th>

                    <c:forEach var="p"
                               items="${compareProducts}">

                        <th>

                            <c:if test="${not empty p.imageUrl}">

                                <img src="${p.imageUrl}"
                                     alt="${p.name}"
                                     class="compare-image"/>

                            </c:if>

                            <br/>

                            <a href="product?id=${p.id}"
                               class="plain-link">
                                <c:out value="${p.name}"/>
                            </a>

                            <br/><br/>

                            <form method="post"
                                  action="compare">

                                <input type="hidden"
                                       name="action"
                                       value="remove"/>

                                <input type="hidden"
                                       name="productId"
                                       value="${p.id}"/>

                                <button class="danger-button">
                                    Убрать
                                </button>

                            </form>

                        </th>

                    </c:forEach>

                </tr>

                <tr class="${compareBrandDifferent ? 'difference-row' : ''}">
                    <td><b>Бренд</b></td>

                    <c:forEach var="p"
                               items="${compareProducts}">

                        <td>
                            <c:out value="${p.brand}"/>
                        </td>

                    </c:forEach>

                </tr>

                <tr class="${comparePriceDifferent ? 'difference-row' : ''}">
                    <td><b>Цена</b></td>

                    <c:forEach var="p"
                               items="${compareProducts}">

                        <td>
                            ${p.price} BYN
                        </td>

                    </c:forEach>

                </tr>

                <tr class="${compareStockDifferent ? 'difference-row' : ''}">
                    <td><b>Наличие</b></td>

                    <c:forEach var="p"
                               items="${compareProducts}">

                        <td>
                            ${p.stock}
                        </td>

                    </c:forEach>

                </tr>

                <tr>
                    <td><b>Рейтинг</b></td>

                    <c:forEach var="p"
                               items="${compareProducts}">

                        <td>

                            <c:choose>

                                <c:when test="${p.reviewsCount > 0}">

                                    <fmt:formatNumber value="${p.averageRating}"
                                                      maxFractionDigits="1"/>
                                    ★
                                    (${p.reviewsCount})

                                </c:when>

                                <c:otherwise>
                                    Нет отзывов
                                </c:otherwise>

                            </c:choose>

                        </td>

                    </c:forEach>

                </tr>

                <tr class="${compareAttributesDifferent ? 'difference-row' : ''}">
                    <td><b>Характеристики</b></td>

                    <c:forEach var="p"
                               items="${compareProducts}">

                        <td>
                            <pre class="compare-attributes"><c:out value="${p.attributes}"/></pre>
                        </td>

                    </c:forEach>

                </tr>

            </table>

        </c:otherwise>

    </c:choose>

</div>

</body>
</html>