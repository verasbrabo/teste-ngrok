package app.enums;

/**
 * Estado do recebimento do pagamento.
 *
 * O pedido nasce como PENDENTE porque o pagamento acontece na entrega. Ao
 * finalizar a entrega ele passa para PAGO; cancelamentos e rejeicoes usam
 * CANCELADO.
 */
public enum StatusPagamento {
    PENDENTE,
    PAGO,
    RECUSADO,
    CANCELADO
}
