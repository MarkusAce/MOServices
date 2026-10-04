package cl.rollerapp.backend.service;

import cl.rollerapp.backend.dto.producto.MecanismoResponse;
import cl.rollerapp.backend.dto.producto.GuardarTelaRequest;
import cl.rollerapp.backend.dto.producto.GuardarMecanismoRequest;
import cl.rollerapp.backend.model.Mecanismo;
import cl.rollerapp.backend.dto.producto.ServicioResponse;
import cl.rollerapp.backend.dto.producto.TelaResponse;
import cl.rollerapp.backend.exception.ApiException;
import cl.rollerapp.backend.model.Tela;
import cl.rollerapp.backend.model.PasoLuz;
import cl.rollerapp.backend.repository.MecanismoRepository;
import cl.rollerapp.backend.repository.ServicioRepository;
import cl.rollerapp.backend.repository.TelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final TelaRepository telaRepository;
    private final MecanismoRepository mecanismoRepository;
    private final ServicioRepository servicioRepository;

    public List<TelaResponse> listarTelas() {
        return telaRepository.findByActivoTrue().stream().map(TelaResponse::desde).toList();
    }

    public List<MecanismoResponse> listarMecanismos() {
        return mecanismoRepository.findByActivoTrue().stream().map(MecanismoResponse::desde).toList();
    }

    public List<ServicioResponse> listarServicios() {
        return servicioRepository.findByActivoTrue().stream().map(ServicioResponse::desde).toList();
    }

    public List<TelaResponse> listarTodasTelas() {
        return telaRepository.findAll().stream().map(TelaResponse::desde).toList();
    }

    public List<MecanismoResponse> listarTodosMecanismos() {
        return mecanismoRepository.findAll().stream().map(MecanismoResponse::desde).toList();
    }

    @Transactional
    public TelaResponse guardarTela(Long id, GuardarTelaRequest req) {
        Tela tela = id == null ? new Tela() : telaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tela no encontrada."));
        tela.setNombre(req.nombre());
        tela.setDescripcion(req.descripcion());
        tela.setPasoLuz(req.pasoLuz());
        tela.setPrecioM2(req.precioM2());
        tela.setActivo(req.activo());
        return TelaResponse.desde(telaRepository.save(tela));
    }

    @Transactional
    public TelaResponse archivarTela(Long id) {
        Tela tela = telaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tela no encontrada."));
        tela.setActivo(false);
        return TelaResponse.desde(telaRepository.save(tela));
    }

    @Transactional
    public MecanismoResponse guardarMecanismo(Long id, GuardarMecanismoRequest req) {
        Mecanismo mecanismo = id == null ? new Mecanismo() : mecanismoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mecanismo no encontrado."));
        mecanismo.setNombre(req.nombre());
        mecanismo.setDescripcion(req.descripcion());
        mecanismo.setValorFijo(req.valorFijo());
        mecanismo.setActivo(req.activo());
        return MecanismoResponse.desde(mecanismoRepository.save(mecanismo));
    }

    @Transactional
    public MecanismoResponse archivarMecanismo(Long id) {
        Mecanismo mecanismo = mecanismoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mecanismo no encontrado."));
        mecanismo.setActivo(false);
        return MecanismoResponse.desde(mecanismoRepository.save(mecanismo));
    }

    @Transactional
    public ServicioResponse actualizarComisionTecnico(Long servicioId, BigDecimal monto) {
        var servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Servicio no encontrado."));
        servicio.setComisionTecnico(monto);
        return ServicioResponse.desde(servicioRepository.save(servicio));
    }

    @Transactional
    public TelaResponse actualizarPrecioTela(Long telaId, BigDecimal nuevoPrecio) {
        Tela tela = telaRepository.findById(telaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tela no encontrada."));

        tela.setPrecioM2(nuevoPrecio);
        return TelaResponse.desde(telaRepository.save(tela));
    }

    @Transactional
    public TelaResponse actualizarPasoLuz(Long telaId, PasoLuz pasoLuz) {
        Tela tela = telaRepository.findById(telaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tela no encontrada."));
        tela.setPasoLuz(pasoLuz);
        return TelaResponse.desde(telaRepository.save(tela));
    }
}
