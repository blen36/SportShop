<%@ page contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" %>

<html>
<head>
    <meta charset="UTF-8">
    <title>Демо платёжный шлюз</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

<div class="container">

    <div class="checkout-box gateway-box">

        <h1>Demo Payment Gateway</h1>

        <p class="muted">
            Это отдельная учебная страница платёжного шлюза.
            SportShop передал сюда только ID платежа и сумму, а не данные карты.
        </p>

        <p><b>Payment ID:</b> ${payment.id}</p>
        <p><b>Order ID:</b> ${payment.orderId}</p>
        <p><b>Amount:</b> ${payment.amount} BYN</p>
        <p><b>Method:</b> ${payment.paymentMethod}</p>

        <form method="post"
              action="payment-gateway">

            <input type="hidden"
                   name="paymentId"
                   value="${payment.id}"/>

            <label>Номер карты</label>
            <br/>
            <input type="text"
                   placeholder="4111 1111 1111 1111"
                   autocomplete="off"
                   required/>

            <br/><br/>

            <label>Срок действия</label>
            <br/>
            <input type="text"
                   placeholder="12/30"
                   autocomplete="off"
                   required/>

            <br/><br/>

            <label>CVV</label>
            <br/>
            <input type="password"
                   placeholder="123"
                   autocomplete="off"
                   required/>

            <br/><br/>

            <label>Результат операции</label>
            <br/>
            <select name="result">
                <option value="success">Успешная оплата</option>
                <option value="fail">Отклонить платёж</option>
            </select>

            <br/><br/>

            <button>
                Отправить результат в SportShop
            </button>

        </form>

    </div>

</div>

</body>
</html>
