package app.enums;

/**
 * Estado do recebimento do pagamento.
 *
 * O pedido nasce como PENDENTE porque o fluxo atual e pagamento na entrega. O
 * status pode evoluir depois quando o funcionario confirmar o recebimento.
 */
public enum StatusPagamento {
    PENDENTE,
    PAGO,
    RECUSADO,
    CANCELADO
}
