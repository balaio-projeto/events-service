package br.com.balaio.events.event.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DetalhesEventoTest {

    @Test
    void cepSomenteComDigitosGanhaOHifen() {
        assertThat(comCep("53020140").cep()).isEqualTo("53020-140");
        assertThat(comCep(" 53020-140 ").cep()).isEqualTo("53020-140");
    }

    @Test
    void camposEmBrancoViramAusentes() {
        DetalhesEvento detalhes = new DetalhesEvento("  ", " Rua da Aurora ", "S/N", "Boa Vista", "",
                "   ", null, " Ana ", "(81) 3333-4444", " ");

        assertThat(detalhes.cep()).isNull();
        assertThat(detalhes.rua()).isEqualTo("Rua da Aurora");
        assertThat(detalhes.numero()).isEqualTo("S/N");
        assertThat(detalhes.complemento()).isNull();
        assertThat(detalhes.referencia()).isNull();
        assertThat(detalhes.contatoNome()).isEqualTo("Ana");
        assertThat(detalhes.contatoEmail()).isNull();
    }

    @Test
    void eventoSemDetalhesUsaDetalhesVaziosEPreservaOsDetalhesAoMudarDeEstado() {
        Evento semDetalhes = EventoTestBuilder.umEvento().comDetalhes(null).build();
        assertThat(semDetalhes.detalhes()).isEqualTo(DetalhesEvento.VAZIO);

        Evento comDetalhes = EventoTestBuilder.umEvento().build();
        Evento submetido = comDetalhes.submetidoPor(UUID.randomUUID(), Instant.now());
        Evento aprovado = submetido.aprovadoPor(UUID.randomUUID(), Instant.now());

        assertThat(submetido.detalhes()).isEqualTo(comDetalhes.detalhes());
        assertThat(aprovado.detalhes()).isEqualTo(comDetalhes.detalhes());
    }

    private static DetalhesEvento comCep(String cep) {
        return new DetalhesEvento(cep, "Rua", "1", "Bairro", null, null, null, "Ana", "81999990000", null);
    }
}
