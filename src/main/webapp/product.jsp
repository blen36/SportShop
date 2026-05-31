<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<html>
<head>

    <meta charset="UTF-8">

    <title>${product.name}</title>

    <link rel="stylesheet" href="styles.css">

</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <a href="products"
       class="reset-link">
        ← Назад в каталог
    </a>

    <br/><br/>

    <c:if test="${param.review == 'success'}">

        <div class="success-message">
            Отзыв добавлен.
        </div>

    </c:if>

    <c:if test="${param.error == 'review'}">

        <div class="error-message">
            Отзыв можно оставить только после покупки товара и только один раз.
        </div>

    </c:if>

    <div class="product-detail">

        <div>

            <c:choose>

                <c:when test="${not empty product.imageUrl}">

                    <img src="${product.imageUrl}"
                         alt="${product.name}"
                         class="product-detail-image"/>

                </c:when>

                <c:otherwise>

                    <div class="product-detail-placeholder">
                        Нет изображения
                    </div>

                </c:otherwise>

            </c:choose>

        </div>

        <div class="section-card">

            <h1>
                <c:out value="${product.name}"/>
            </h1>

            <p class="product-description-large">
                <c:out value="${product.description}"/>
            </p>

            <div class="product-meta-grid">

                <div>
                    <span class="meta-label">Бренд</span>
                    <b>
                        <c:choose>
                            <c:when test="${not empty product.brand}">
                                <c:out value="${product.brand}"/>
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </b>
                </div>

                <div>
                    <span class="meta-label">Рейтинг</span>
                    <b class="rating">
                        ★
                        <c:choose>
                            <c:when test="${product.reviewsCount > 0}">
                                <fmt:formatNumber value="${product.averageRating}"
                                                  maxFractionDigits="1"/>
                            </c:when>
                            <c:otherwise>
                                <span class="muted">Нет отзывов</span>
                            </c:otherwise>
                        </c:choose>
                    </b>
                </div>

                <div>
                    <span class="meta-label">В наличии</span>
                    <b>${product.stock}</b>
                </div>

            </div>

            <p class="price">
                ${product.price} BYN
            </p>

            <c:if test="${product.stock > 0 && product.stock <= 5}">

                <p class="warning-text">
                    Осталось мало товара ⚠
                </p>

            </c:if>

            <c:if test="${not empty product.attributeItems}">

                <div class="attributes-box clean-attributes">
                    <h3>Характеристики</h3>

                    <div class="attributes-list">

                        <c:forEach var="attr"
                                   items="${product.attributeItems}">

                            <div class="attribute-row">
                                <span class="attribute-key">
                                    <c:out value="${attr.key}"/>
                                </span>
                                <span class="attribute-value">
                                    <c:out value="${attr.value}"/>
                                </span>
                            </div>

                        </c:forEach>

                    </div>
                </div>

            </c:if>

            <div class="card-actions">

                <form method="post"
                      action="cart"
                      class="inline-form">

                    <input type="hidden"
                           name="productId"
                           value="${product.id}"/>

                    <input type="hidden"
                           name="action"
                           value="add"/>

                    <c:choose>

                        <c:when test="${product.stock <= 0}">

                            <button disabled
                                    class="disabled-button">
                                Нет в наличии
                            </button>

                        </c:when>

                        <c:otherwise>

                            <button>
                                Добавить в корзину
                            </button>

                        </c:otherwise>

                    </c:choose>

                </form>

                <form method="post"
                      action="favorite"
                      class="inline-form">

                    <input type="hidden"
                           name="productId"
                           value="${product.id}"/>

                    <input type="hidden"
                           name="action"
                           value="add"/>

                    <button type="submit"
                            class="secondary-button">
                        В избранное ❤️
                    </button>

                </form>

                <form method="post"
                      action="compare"
                      class="inline-form">

                    <input type="hidden"
                           name="productId"
                           value="${product.id}"/>

                    <input type="hidden"
                           name="action"
                           value="add"/>

                    <button type="submit"
                            class="secondary-button">
                        Добавить к сравнению
                    </button>

                </form>

            </div>

        </div>

    </div>

    <br/>

    <div class="section-card">

        <h2>Отзывы</h2>

        <c:choose>

            <c:when test="${canReview}">

                <form method="post"
                      action="review"
                      class="review-form">

                    <input type="hidden"
                           name="productId"
                           value="${product.id}"/>

                    <label>
                        Оценка
                    </label>

                    <br/>

                    <select name="rating"
                            required>

                        <option value="5">5 — Отлично</option>
                        <option value="4">4 — Хорошо</option>
                        <option value="3">3 — Нормально</option>
                        <option value="2">2 — Плохо</option>
                        <option value="1">1 — Очень плохо</option>

                    </select>

                    <br/><br/>

                    <label>
                        Комментарий
                    </label>

                    <br/>

                    <textarea name="comment"
                              rows="4"
                              placeholder="Напишите отзыв о товаре"></textarea>

                    <br/><br/>

                    <button>
                        Оставить отзыв
                    </button>

                </form>

                <br/>

            </c:when>

            <c:otherwise>

                <p class="muted">
                    Оставить отзыв можно после покупки товара.
                </p>

            </c:otherwise>

        </c:choose>

        <c:choose>

            <c:when test="${empty reviews}">

                <p>
                    Отзывов пока нет.
                </p>

            </c:when>

            <c:otherwise>

                <c:forEach var="r"
                           items="${reviews}">

                    <div class="review-card">

                        <p class="rating">
                            ★ ${r.rating}
                        </p>

                        <p>
                            <b>
                                <c:out value="${r.userEmail}"/>
                            </b>
                        </p>

                        <p>
                            <c:out value="${r.comment}"/>
                        </p>

                        <p class="muted">
                            ${r.createdAt}
                        </p>

                    </div>

                </c:forEach>

            </c:otherwise>

        </c:choose>

    </div>

    <br/>

    <div class="section-card">

        <h2>Похожие товары</h2>

        <c:choose>

            <c:when test="${empty relatedProducts}">
                <p>Похожих товаров пока нет.</p>
            </c:when>

            <c:otherwise>

                <div class="products">

                    <c:forEach var="p"
                               items="${relatedProducts}">

                        <div class="product-card small-card">

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

                            <p class="price">
                                ${p.price} BYN
                            </p>

                        </div>

                    </c:forEach>

                </div>

            </c:otherwise>

        </c:choose>

    </div>

    <br/>

    <div class="section-card">

        <h2>Популярные товары</h2>

        <div class="products">

            <c:forEach var="p"
                       items="${popularProducts}">

                <div class="product-card small-card">

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

                    <p class="price">
                        ${p.price} BYN
                    </p>

                </div>

            </c:forEach>

        </div>

    </div>

</div>

</body>
</html>
