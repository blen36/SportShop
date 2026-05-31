<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Корзина</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="container">

    <h1>Корзина 🛒</h1>

    <c:if test="${param.error == 'stock'}">
        <div class="error-message">
            Недостаточно товара на складе или превышен лимит количества.
        </div>
    </c:if>

    <c:if test="${param.error == 'checkout'}">
        <div class="error-message">
            Не удалось оформить заказ. Проверьте адрес доставки и наличие товаров.
        </div>
    </c:if>

    <c:if test="${param.error == 'promo'}">
        <div class="error-message">
            Промокод недействителен или срок его действия истёк.
        </div>
    </c:if>

    <c:choose>

        <c:when test="${empty cart}">

            <div class="empty-message">
                Корзина пуста.
                <br/><br/>
                <a href="products">Перейти в каталог</a>
            </div>

        </c:when>

        <c:otherwise>

            <table class="data-table">

                <tr>
                    <th>Товар</th>
                    <th>Цена с учётом скидок</th>
                    <th>Количество</th>
                    <th>Сумма</th>
                    <th>Действия</th>
                </tr>

                <c:forEach var="item"
                           items="${cart}">

                    <tr>

                        <td>
                            <c:out value="${item.productName}"/>
                        </td>

                        <td>
                            ${item.price} BYN
                        </td>

                        <td>

                            <form method="post"
                                  action="cart"
                                  class="inline-form">

                                <input type="hidden"
                                       name="action"
                                       value="update"/>

                                <input type="hidden"
                                       name="productId"
                                       value="${item.productId}"/>

                                <input type="number"
                                       name="quantity"
                                       value="${item.quantity}"
                                       min="1"
                                       max="10"
                                       class="qty-input"/>

                                <button>
                                    Обновить
                                </button>

                            </form>

                        </td>

                        <td>
                            ${item.total} BYN
                        </td>

                        <td>

                            <form method="post"
                                  action="cart"
                                  class="inline-form">

                                <input type="hidden"
                                       name="action"
                                       value="remove"/>

                                <input type="hidden"
                                       name="productId"
                                       value="${item.productId}"/>

                                <button class="danger-button">
                                    Удалить
                                </button>

                            </form>

                        </td>

                    </tr>

                </c:forEach>

            </table>

            <br/>

            <div class="checkout-box">

                <h2>Оформление заказа</h2>

                <p>
                    <b>Сумма товаров:</b>
                    ${cartTotal} BYN
                </p>

                <p class="muted">
                    Скидки на товары уже учтены в цене. Промокод применяется дополнительно к сумме товаров.
                </p>

                <p class="muted">
                    Стандартная доставка стоит 10 BYN.
                    Для заказов от 200 BYN стандартная доставка бесплатная.
                    Экспресс-доставка стоит 20 BYN.
                    Самовывоз бесплатный.
                </p>

                <form method="post"
                      action="orders">

                    <label>
                        Способ доставки
                    </label>

                    <br/>

                    <select name="deliveryType"
                            required>

                        <option value="STANDARD">
                            Стандартная доставка
                        </option>

                        <option value="EXPRESS">
                            Экспресс-доставка
                        </option>

                        <option value="PICKUP">
                            Самовывоз
                        </option>

                    </select>

                    <br/><br/>

                    <label>
                        Адрес доставки
                    </label>

                    <p class="muted">
                        Для самовывоза можно оставить поле пустым.
                    </p>

                    <textarea name="deliveryAddress"
                              rows="4"
                              placeholder="Введите город, улицу, дом, квартиру"></textarea>

                    <br/><br/>

                    <label>
                        Промокод
                    </label>

                    <br/>

                    <input type="text"
                           name="promoCode"
                           placeholder="Например: SPORT10"/>

                    <br/><br/>

                    <button>
                        Оформить заказ
                    </button>

                </form>

            </div>

        </c:otherwise>

    </c:choose>

</div>

</body>
</html>