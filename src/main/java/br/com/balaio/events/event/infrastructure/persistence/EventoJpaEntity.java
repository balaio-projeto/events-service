package br.com.balaio.events.event.infrastructure.persistence;

import br.com.balaio.events.event.domain.Evento;
import br.com.balaio.events.event.domain.StatusAprovacao;
import br.com.balaio.events.event.domain.StatusPublicacao;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "eventos")
public class EventoJpaEntity {

    protected EventoJpaEntity(){}

    public EventoJpaEntity(Evento evento) {
        this.id = evento.id();
        this.slug = evento.slug();
        this.nome = evento.nome();
        this.tipo = evento.tipo();
        this.organizador = evento.organizador();
        this.descricao = evento.descricao();
        this.statusAprovacao = evento.statusAprovacao();
        this.statusPublicacao = evento.statusPublicacao();
        this.dataInicio = evento.dataInicio();
        this.dataFim = evento.dataFim();
        this.horarioInicio = evento.horarioInicio();
        this.horarioFim = evento.horarioFim();
        this.entrada = evento.entrada();
        this.cidade = evento.cidade();
        this.local = evento.local();
        this.endereco = evento.endereco();
        this.latitude = evento.latitude();
        this.longitude = evento.longitude();
        this.periodo = evento.periodo();
        this.horario = evento.horario();
        this.artesaos = evento.artesaos();
        this.coletivos = evento.coletivos();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug",nullable = false,unique = true)
    private String slug;

    @Column(name = "nome",nullable = false)
    private String nome;

    @Column(name = "tipo",nullable = false,length = 130)
    private String tipo;

    @Column(name = "organizador",nullable = false)
    private String organizador;

    @Column(name = "descricao",columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", nullable = false,length = 50)
    private StatusAprovacao statusAprovacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_publicacao", nullable = false,length = 50)
    private StatusPublicacao statusPublicacao;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "horario_inicio")
    private LocalTime horarioInicio;

    @Column(name = "horario_fim")
    private LocalTime horarioFim;

    @Column(name = "entrada")
    private String entrada;

    @Column(name = "cidade", nullable = false, length = 150)
    private String cidade;

    @Column(name = "local", nullable = false)
    private String local;

    @Column(name = "endereco", nullable = false, length = 500)
    private String endereco;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "periodo", nullable = false, columnDefinition = "TEXT")
    private String periodo;

    @Column(name = "horario", columnDefinition = "TEXT")
    private String horario;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "artesaos", nullable = false, columnDefinition = "jsonb")
    private List<String> artesaos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coletivos", nullable = false, columnDefinition = "jsonb")
    private List<String> coletivos;

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getOrganizador() {
        return organizador;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusAprovacao getStatusAprovacao() {
        return statusAprovacao;
    }

    public StatusPublicacao getStatusPublicacao() {
        return statusPublicacao;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public String getEntrada() {
        return entrada;
    }

    public String getCidade() {
        return cidade;
    }

    public String getLocal() {
        return local;
    }

    public String getEndereco() {
        return endereco;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getPeriodo() {
        return periodo;
    }

    public String getHorario() {
        return horario;
    }

    public List<String> getArtesaos() {
        return artesaos;
    }

    public List<String> getColetivos() {
        return coletivos;
    }
}
