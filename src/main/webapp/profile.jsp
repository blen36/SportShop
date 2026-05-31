<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Личный кабинет</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Личный кабинет</h1>

    <div class="section-card">

        <h2>
            ${user.email}
        </h2>

        <p>
            <b>Роль:</b>
            ${user.role}
        </p>

        <p>
            <b>Дата регистрации:</b>
            ${user.createdAt}
        </p>

        <a href="orders"
           class="button-link">
            Мои заказы 📦
        </a>

        <a href="compare"
           class="button-link secondary-link">
            Сравнение товаров
        </a>

    </div>

    <br/>

    <div class="section-card">

        <div class="section-header">

            <h2>Уведомления</h2>

            <c:if test="${unreadNotifications > 0}">
                <span class="notification-count">
                    ${unreadNotifications} новых
                </span>
            </c:if>

        </div>

        <c:choose>

            <c:when test="${empty notifications}">
                <p>Уведомлений пока нет.</p>
            </c:when>

            <c:otherwise>

                <form method="post"
                      action="notifications">

                    <input type="hidden"
                           name="action"
                           value="markRead"/>

                    <button class="secondary-button">
                        Отметить все как прочитанные
                    </button>

                </form>

                <br/>

                <c:forEach var="n"
                           items="${notifications}">

                    <div class="${n.read ? 'notification-card' : 'notification-card unread'}">

                        <p>
                            <c:out value="${n.message}"/>
                        </p>

                        <p class="muted">
                            ${n.createdAt}
                        </p>

                    </div>

                </c:forEach>

            </c:otherwise>

        </c:choose>

    </div>

    <br/>

    <div class="section-card">

        <h2>История заказов</h2>

        <c:choose>

            <c:when test="${empty orders}">

                <p>
                    Заказов пока нет.
                </p>

            </c:when>

            <c:otherwise>

                <table class="data-table">

                    <tr>
                        <th>ID</th>
                        <th>Статус</th>
                        <th>Сумма</th>
                        <th>Скидка</th>
                        <th>Доставка</th>
                        <th>Дата</th>
                        <th>История статусов</th>
                    </tr>

                    <c:forEach var="o"
                               items="${orders}">

                        <tr>
                            <td>#${o.id}</td>
                            <td>${o.status}</td>
                            <td>${o.totalPrice} BYN</td>
                            <td>${o.discountAmount} BYN</td>
                            <td>${o.deliveryPrice} BYN</td>
                            <td>${o.createdAt}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty orderStatusHistory[o.id]}">
                                        -
                                    </c:when>
                                    <c:otherwise>
                                        <ul class="history-list">
                                            <c:forEach var="h" items="${orderStatusHistory[o.id]}">
                                                <li>
                                                    ${h.createdAt}: ${h.oldStatus == null ? '—' : h.oldStatus}
                                                    → ${h.newStatus}
                                                    <c:if test="${not empty h.comment}">
                                                        <br/><span class="muted"><c:out value="${h.comment}"/></span>
                                                    </c:if>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>

                    </c:forEach>

                </table>

            </c:otherwise>

        </c:choose>

    </div>

    <br/>

    <div class="section-card">

        <h2>Избранное ❤️</h2>

        <c:choose>

            <c:when test="${empty favorites}">

                <p>
                    Избранных товаров пока нет.
                </p>

            </c:when>

            <c:otherwise>

                <div class="products">

                    <c:forEach var="p"
                               items="${favorites}">

                        <div class="product-card">

                            <c:if test="${not empty p.imageUrl}">
                                <img src="${p.imageUrl}"
                                     alt="${p.name}"
                                     class="product-image"/>
                            </c:if>

                            <h2>
                                <a href="product?id=${p.id}"
                                   class="plain-link">
                                    <c:out value="${p.name}"/>
                                </a>
                            </h2>

                            <p>
                                <c:out value="${p.description}"/>
                            </p>

                            <p>
                                <b>Цена:</b>
                                ${p.price} BYN
                            </p>

                            <form method="post"
                                  action="favorite">

                                <input type="hidden"
                                       name="productId"
                                       value="${p.id}"/>

                                <input type="hidden"
                                       name="action"
                                       value="remove"/>

                                <button class="danger-button">
                                    Удалить из избранного
                                </button>

                            </form>

                        </div>

                    </c:forEach>

                </div>

            </c:otherwise>

        </c:choose>

    </div>

    <br/>

    <div class="section-card">

        <h2>Недавно просмотренные товары</h2>

        <c:choose>

            <c:when test="${empty recentlyViewed}">

                <p>
                    История просмотров пока пуста.
                </p>

            </c:when>

            <c:otherwise>

                <div class="products">

                    <c:forEach var="p"
                               items="${recentlyViewed}">

                        <div class="product-card small-card">

                            <c:if test="${not empty p.imageUrl}">
                                <img src="${p.imageUrl}"
                                     alt="${p.name}"
                                     class="product-image"/>
                            </c:if>

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

        <h2>Рекомендации для вас</h2>

        <c:choose>

            <c:when test="${empty recommendations}">

                <p>
                    Рекомендаций пока нет.
                </p>

            </c:when>

            <c:otherwise>

                <div class="products">

                    <c:forEach var="p"
                               items="${recommendations}">

                        <div class="product-card small-card">

                            <c:if test="${not empty p.imageUrl}">
                                <img src="${p.imageUrl}"
                                     alt="${p.name}"
                                     class="product-image"/>
                            </c:if>

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

</div>

</body>
</html>