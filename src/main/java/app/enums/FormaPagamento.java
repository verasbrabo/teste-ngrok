package app.enums;

/**
 * Forma escolhida pelo cliente antes de criar o pedido.
 *
 * Este enum representa intencao de pagamento na entrega, nao pagamento online
 * confirmado. Por isso Pix/cartao/dinheiro entram inicialmente como pendentes.
 */
public enum FormaPagamento {
    DINHEIRO,
    PIX,
    CARTAO_CREDITO,
    CARTAO_DEBITO;

    /**
     * Indica se o entregador precisa levar a maquininha para receber o pedido.
     */
    public boolean precisaMaquininha() {
        return this == CARTAO_CREDITO || this == CARTAO_DEBITO;
    }
}