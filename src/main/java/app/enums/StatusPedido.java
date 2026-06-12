package app.enums;

import java.util.List;

import lombok.Getter;

public enum StatusPedido {
	REJEITADO("Infelizmente, seu pedido foi cancelado pelo restaurante."),
    CRIADO("Seu pedido foi recebido pelo restaurante!"),
    ACEITO("Seu pedido foi aceito pelo restaurante!"),
    PREPARANDO("O restaurante confirmou seu pedido e começou a preparar."),
    EM_ENTREGA("Boa notícia! Seu pedido já saiu para a entrega."),
    FINALIZADO("Pedido entregue. Bom apetite!"),
    AVALIADO("Pedido avaliado com sucesso."),
    CANCELADO("Infelizmente, seu pedido foi cancelado pelo restaurante.");

    public static final List<StatusPedido> STATUS_FINALIZADOS =
    List.of(REJEITADO, FINALIZADO, AVALIADO, CANCELADO);

    public boolean podeIrPara(StatusPedido novoStatus) {

        return switch (this) {

            case CRIADO ->
                novoStatus == ACEITO ||
                novoStatus == CANCELADO  ||
                novoStatus == REJEITADO;

            case ACEITO ->
                novoStatus == PREPARANDO ||
                novoStatus == CANCELADO;

            case PREPARANDO ->
                novoStatus == EM_ENTREGA ||
                novoStatus == CANCELADO;

            case EM_ENTREGA ->
//                novoStatus == FINALIZADO ||
                novoStatus == CANCELADO;

            case REJEITADO, FINALIZADO, AVALIADO, CANCELADO ->
                false;
        };
    }
    
    private final String descricao;

    private StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return this.descricao;
    }

}

