package com.antigravity.assistente.usuario;

import com.antigravity.assistente.usuario.dto.UsuarioMapper;
import com.antigravity.assistente.usuario.dto.UsuarioRequest;
import com.antigravity.assistente.usuario.dto.UsuarioResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários — UsuarioService
 * 
 * Cobertura completa das regras de negócio:
 *  - Cadastro (sucesso, e-mail duplicado, nível inválido, todos os níveis válidos)
 *  - Busca por ID (encontrado / não encontrado)
 *  - Listagem (vazia / com registros)
 *  - Busca por nome
 *  - Atualização (sucesso, e-mail conflitante, mesmo e-mail)
 *  - Exclusão (sucesso / inexistente)
 * 
 * @author Antigravity Team
 * @version 2.0 (corrigida)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Regras de Negócio")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    // Mapper real (não precisa de mock — é lógica pura sem I/O)
    private final UsuarioMapper mapper = new UsuarioMapper();

    // Service construído manualmente para evitar problemas de @InjectMocks + @Spy
    private UsuarioService usuarioService;

    private Usuario usuarioExistente;
    private UsuarioRequest requestValido;

    @BeforeEach
    void setUp() {
        // Construção manual garante injeção correta do mapper real + mock do repo
        usuarioService = new UsuarioService(usuarioRepository, mapper);

        usuarioExistente = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .nivelAcesso("OPERADOR")
                .criadoEm(LocalDateTime.of(2026, 7, 22, 10, 0))
                .atualizadoEm(LocalDateTime.of(2026, 7, 22, 10, 0))
                .build();

        requestValido = new UsuarioRequest("João Silva", "joao@email.com", "OPERADOR");
    }

    // ==================== CADASTRAR ====================

    @Nested
    @DisplayName("cadastrar()")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar usuário com sucesso quando dados válidos")
        void deveCadastrarComSucesso() {
            when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

            UsuarioResponse response = usuarioService.cadastrar(requestValido);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nome()).isEqualTo("João Silva");
            assertThat(response.email()).isEqualTo("joao@email.com");
            assertThat(response.nivelAcesso()).isEqualTo("OPERADOR");

            verify(usuarioRepository).existsByEmail("joao@email.com");
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve rejeitar cadastro quando e-mail já existe")
        void deveRejeitarEmailDuplicado() {
            when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.cadastrar(requestValido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Já existe um usuário cadastrado");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar cadastro quando nível de acesso é inválido")
        void deveRejeitarNivelInvalido() {
            UsuarioRequest requestInvalido = new UsuarioRequest("Ana", "ana@email.com", "SUPERUSER");
            when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.cadastrar(requestInvalido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nível de acesso inválido");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve aceitar todos os níveis válidos: ADMIN, GERENTE, OPERADOR, VISUALIZADOR")
        void deveAceitarTodosNiveisValidos() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

            List<String> niveis = List.of("ADMIN", "GERENTE", "OPERADOR", "VISUALIZADOR");
            for (String nivel : niveis) {
                UsuarioRequest req = new UsuarioRequest("Teste", "teste_" + nivel + "@email.com", nivel);
                assertThatCode(() -> usuarioService.cadastrar(req))
                        .doesNotThrowAnyException();
            }

            verify(usuarioRepository, times(4)).save(any(Usuario.class));
        }
    }

    // ==================== BUSCAR ====================

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar usuário quando ID existe")
        void deveRetornarQuandoExiste() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

            UsuarioResponse response = usuarioService.buscarPorId(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("joao@email.com");
            verify(usuarioRepository).findById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID não existe")
        void deveLancarExcecaoQuandoNaoExiste() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarPorId(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários")
        void deveRetornarListaVazia() {
            when(usuarioRepository.findAll()).thenReturn(List.of());

            List<UsuarioResponse> resultado = usuarioService.listarTodos();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar todos os usuários cadastrados")
        void deveRetornarTodos() {
            Usuario segundo = Usuario.builder()
                    .id(2L).nome("Maria").email("maria@email.com")
                    .nivelAcesso("ADMIN")
                    .criadoEm(LocalDateTime.of(2026, 7, 22, 11, 0))
                    .atualizadoEm(LocalDateTime.of(2026, 7, 22, 11, 0))
                    .build();

            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioExistente, segundo));

            List<UsuarioResponse> resultado = usuarioService.listarTodos();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).nome()).isEqualTo("João Silva");
            assertThat(resultado.get(1).nome()).isEqualTo("Maria");
        }
    }

    @Nested
    @DisplayName("buscarPorNome()")
    class BuscarPorNome {

        @Test
        @DisplayName("Deve buscar por nome parcial (case-insensitive)")
        void deveBuscarPorNome() {
            when(usuarioRepository.findByNomeContainingIgnoreCase("João"))
                    .thenReturn(List.of(usuarioExistente));

            List<UsuarioResponse> resultado = usuarioService.buscarPorNome("João");

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nome()).isEqualTo("João Silva");
        }
    }

    @Nested
    @DisplayName("buscarPorNivelAcesso()")
    class BuscarPorNivel {

        @Test
        @DisplayName("Deve filtrar usuários por nível de acesso")
        void deveFiltrarPorNivel() {
            when(usuarioRepository.findByNivelAcesso("OPERADOR"))
                    .thenReturn(List.of(usuarioExistente));

            List<UsuarioResponse> resultado = usuarioService.buscarPorNivelAcesso("operador");

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nivelAcesso()).isEqualTo("OPERADOR");
        }
    }

    // ==================== ATUALIZAR ====================

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar usuário com sucesso mantendo mesmo e-mail")
        void deveAtualizarComSucesso() {
            UsuarioRequest requestAtualizado = new UsuarioRequest(
                    "João Atualizado", "joao@email.com", "GERENTE"
            );

            // O save retorna o objeto já modificado pelo mapper
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation ->
                    invocation.getArgument(0) // retorna o próprio objeto passado ao save
            );

            UsuarioResponse response = usuarioService.atualizar(1L, requestAtualizado);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("João Atualizado");
            assertThat(response.nivelAcesso()).isEqualTo("GERENTE");
            verify(usuarioRepository).save(any(Usuario.class));
            // Não deve chamar existsByEmail pois o e-mail não mudou
            verify(usuarioRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Deve permitir atualizar para um novo e-mail disponível")
        void devePermitirNovoEmailDisponivel() {
            UsuarioRequest requestNovoEmail = new UsuarioRequest(
                    "João Silva", "joao.novo@email.com", "OPERADOR"
            );

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.existsByEmail("joao.novo@email.com")).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation ->
                    invocation.getArgument(0)
            );

            UsuarioResponse response = usuarioService.atualizar(1L, requestNovoEmail);

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("joao.novo@email.com");
            verify(usuarioRepository).existsByEmail("joao.novo@email.com");
        }

        @Test
        @DisplayName("Deve rejeitar atualização quando novo e-mail já está em uso")
        void deveRejeitarEmailConflitante() {
            UsuarioRequest requestNovoEmail = new UsuarioRequest(
                    "João", "outro@email.com", "OPERADOR"
            );

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.existsByEmail("outro@email.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.atualizar(1L, requestNovoEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("já está em uso");

            verify(usuarioRepository, never()).save(any());
        }
    }

    // ==================== DELETAR ====================

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("Deve deletar usuário quando ID existe")
        void deveDeletarComSucesso() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

            usuarioService.deletar(1L);

            verify(usuarioRepository).delete(usuarioExistente);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar ID inexistente")
        void deveLancarExcecaoQuandoNaoExiste() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.deletar(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrado");

            verify(usuarioRepository, never()).delete(any());
        }
    }
}
