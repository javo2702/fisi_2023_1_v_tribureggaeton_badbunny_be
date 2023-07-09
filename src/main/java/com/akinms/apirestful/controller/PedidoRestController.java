package com.akinms.apirestful.controller;

import com.akinms.apirestful.business.IPedidoBusiness;
import com.akinms.apirestful.entity.DetallePedido;
import com.akinms.apirestful.entity.Pedido;
import com.akinms.apirestful.entity.PedidoPayment;
import com.akinms.apirestful.exception.BusinessException;
import com.akinms.apirestful.exception.NotFoundException;
import com.akinms.apirestful.responseentity.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/atencioncliente/v1/pedidos")
public class PedidoRestController {

    @Autowired
    private IPedidoBusiness pedidoBusiness;

    @GetMapping("/consultar/cliente/{id}")
    public ResponseEntity<RespuestaPedido> listPedidosCliente(@PathVariable Long id){
        RespuestaPedido respuestaPedido = new RespuestaPedido();
        try{
            List<Pedido> pedidosCliente = pedidoBusiness.getPedidosCliente(id);
            List<Pedidos> pedidosClienteResponse = new ArrayList<>();
            //List<DetallePedidos> detallesResponse = new ArrayList<>();
            for(Pedido p : pedidosCliente){
                Pedidos ped = new Pedidos();
                List<DetallePedidos> detallesResponse = new ArrayList<>();
                ped.setIdpedido(p.getIdpedido());
                for(DetallePedido dt : p.getDetallesPedido()){
                    System.out.println("CANTIDAD DE PRODUCTOS DETALLE: "+p.getDetallesPedido().size());
                    DetallePedidos det = new DetallePedidos();
                    det.setPedido("00");
                    det.setCantidad(dt.getCantidad());
                    det.setProducto(dt.getProducto());
                    detallesResponse.add(det);
                }
                ped.setDetallesPedido(detallesResponse);
                ped.setEstado(p.getEstado());
                ped.setFecha(p.getFecha().toString().substring(0,10));
                ped.setMetodo_pago(p.getMetodo_pago());
                ped.setMonto_total(p.getMonto_total());
                ped.setTipo_entrega(p.getTipo_entrega());
                BodegaUbicacion bu = new BodegaUbicacion();
                bu.setNombre(p.getBodega().getNombre());
                bu.setIdbodega(p.getBodega().getIdbodega());
                bu.setLongitud(p.getBodega().getUbicacion().getLongitud());
                bu.setLatitud(p.getBodega().getUbicacion().getLatitud());
                bu.setDireccion(p.getBodega().getUbicacion().getNombre());
                ped.setBodega(bu);
                pedidosClienteResponse.add(ped);
            }
            if(pedidosClienteResponse.size()>0)
                respuestaPedido.setMensaje("Cantidad de pedidos: "+pedidosClienteResponse.size());
            else
                respuestaPedido.setMensaje("No Tiene pedidos registrados");
            respuestaPedido.setPedidos(pedidosClienteResponse);
            return new ResponseEntity<>(respuestaPedido,HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/consultar/bodega/{id}")
    public ResponseEntity<RespuestaPedido> listPedidosBodega(@PathVariable Long id){
        RespuestaPedido respuestaPedido = new RespuestaPedido();
        try{
            List<Pedido> pedidosBodega = pedidoBusiness.getPedidosBodega(id);
            if(pedidosBodega.size()>0)
                respuestaPedido.setMensaje("Cantidad de pedidos: "+pedidosBodega.size());
            else
                respuestaPedido.setMensaje("No Tiene pedidos registrados");
            respuestaPedido.setPedidos(pedidosBodega);
            return new ResponseEntity<>(respuestaPedido,HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/consultar/ventas/bodega/{id}/fechas")
    public ResponseEntity<RespuestaVentas> getVentasSemanales(@PathVariable Long id, @RequestParam String fecha_inicio, @RequestParam String fecha_fin){
        RespuestaVentas respuestaVentas = new RespuestaVentas();
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //Ventas ventasSemanales = pedidoBusiness.getVentasSemanes(sdf.parse(fecha_inicio),sdf.parse(fecha_fin),id);
            List<Pedido> ventasSemanales = pedidoBusiness.getVentasSemanes(fecha_inicio,fecha_fin,id);
            List<Ventas> ventas = new ArrayList<>();
            for(Pedido p : ventasSemanales){
                Ventas v = new Ventas();
                v.setMonto(p.getMonto_total());
                String feccha = p.getFecha().toString();
                v.setFecha(feccha.substring(0,10));
                ventas.add(v);
            }
            respuestaVentas.setMensaje("Ventas semanales en el rango: ["+fecha_inicio+" - "+fecha_fin+"]");
            respuestaVentas.setVentas(ventas);
            return new ResponseEntity<>(respuestaVentas,HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/registrar")
    public ResponseEntity<?> save(@RequestBody PedidoPayment pedido){
        RespuestaPedido respuestaPedido = new RespuestaPedido();
        respuestaPedido.setMensaje("Error al registrar pedido");
        try{
            //llamar stripe
            /*Stripe.apiKey = "sk_test_51NR4G8IpwXHFWzNeGpbrn9HNgSaHN1uNw4NqdCJqcxY9WfRvRJ1Yq3poqQnO8EHT7Fl0qom9gzRhcExv7tGfB5u500o4EGuQgx";
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", pedido.getPayment().getAmount());
            chargeParams.put("currency", pedido.getPayment().getCurrency());*/
            /*chargeParams.put("source",pedido.getPayment().getToken());
            chargeParams.put("exp_month", pedido.getPayment().getExpMonth());
            chargeParams.put("exp_year", pedido.getPayment().getExpYear());
            chargeParams.put("cvc", pedido.getPayment().getCvv());*/

            /*Map<String, Object> cardParams = new HashMap<>();
            cardParams.put("number", pedido.getPayment().getCardNumber());
            cardParams.put("exp_month", pedido.getPayment().getExpMonth());
            cardParams.put("exp_year", pedido.getPayment().getExpYear());
            cardParams.put("cvc", pedido.getPayment().getCvv());
            chargeParams.put("card", cardParams);

            // Crea el cargo utilizando la API de Stripe
            //Charge charge = Charge.create(chargeParams);*/
            //confirmacion
            Pedido pedidoTemp = new Pedido(
                    pedido.getFecha(),
                    pedido.getMonto_total(),
                    pedido.getEstado(),
                    pedido.getTipo_entrega(),
                    pedido.getMetodo_pago(),
                    pedido.getCliente(),
                    pedido.getBodega()
                    //pedido.getDetallesPedido()
                    );
            pedidoTemp.setDetallesPedido(pedido.getDetallesPedido());
            //System.out.println(pedido.getFecha());
            Pedido pedidoSave = pedidoBusiness.save(pedidoTemp);
            respuestaPedido.setMensaje("Pedido registrado de manera exitosa");
            respuestaPedido.setPedidos(pedidoSave);
            return new ResponseEntity<>(respuestaPedido,HttpStatus.CREATED);
            /*EntityModel<Pedido> entityModel = assemblerPedido.toModel(pedidoBusiness.save(pedido));
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);*/
        }catch (BusinessException e){
            return new ResponseEntity<>(respuestaPedido,HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (NotFoundException e){
            return new ResponseEntity<>(respuestaPedido,HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/consultar/cliente/{id_cliente}/pedido/{id_pedido}")
    public ResponseEntity<RespuestaPedido> detallePedidoCliente(@PathVariable Long id_cliente, @PathVariable Long id_pedido) {
        RespuestaPedido respuestaPedido = new RespuestaPedido();
        try{
            Pedido pedidoCliente = pedidoBusiness.getDetallePedidoCliente(id_cliente,id_pedido);
            Pedidos pedidoClienteResponse = new Pedidos();

            List<DetallePedidos> detallesResponse = new ArrayList<>();
            pedidoClienteResponse.setIdpedido(pedidoCliente.getIdpedido());
            for (DetallePedido dt : pedidoCliente.getDetallesPedido()) {
                System.out.println("CANTIDAD DE PRODUCTOS DETALLE: "+pedidoCliente.getDetallesPedido().size());
                DetallePedidos det = new DetallePedidos();
                det.setPedido("00");
                det.setCantidad(dt.getCantidad());
                det.setProducto(dt.getProducto());
                detallesResponse.add(det);
            }

            pedidoClienteResponse.setDetallesPedido(detallesResponse);
            pedidoClienteResponse.setEstado(pedidoCliente.getEstado());
            pedidoClienteResponse.setFecha(pedidoCliente.getFecha().toString().substring(0,10));
            pedidoClienteResponse.setMetodo_pago(pedidoCliente.getMetodo_pago());
            pedidoClienteResponse.setMonto_total(pedidoCliente.getMonto_total());
            pedidoClienteResponse.setTipo_entrega(pedidoCliente.getTipo_entrega());
                BodegaUbicacion bu = new BodegaUbicacion();
                bu.setNombre(pedidoCliente.getBodega().getNombre());
                bu.setIdbodega(pedidoCliente.getBodega().getIdbodega());
                bu.setLongitud(pedidoCliente.getBodega().getUbicacion().getLongitud());
                bu.setLatitud(pedidoCliente.getBodega().getUbicacion().getLatitud());
                bu.setDireccion(pedidoCliente.getBodega().getUbicacion().getNombre());
            pedidoClienteResponse.setBodega(bu);

            respuestaPedido.setMensaje("Pedido obtenido con id: "+id_pedido);
            respuestaPedido.setPedidos(pedidoClienteResponse);

            return new ResponseEntity<>(respuestaPedido,HttpStatus.OK);

        }catch (BusinessException e){
            return new ResponseEntity<>(respuestaPedido,HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (NotFoundException e){
            return new ResponseEntity<>(respuestaPedido,HttpStatus.NOT_FOUND);
        }catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    @PutMapping("/actualizarestado/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Pedido pedido){
        RespuestaMensaje respuesta = new RespuestaMensaje();
        respuesta.setMensaje("Error al actualizar el estado del pedido");
        //String respuesta = "Error al actualizar el estado del pedido";
        //respuesta.setMensaje("Error al actualizar el estado del pedido");
        try {
            Pedido pedidoActualizado = pedidoBusiness.updateEstadoPedido(pedido.getEstado(),id);
            respuesta.setMensaje("Se ha actualizado el estado del pedido de manera exitosa");
            //respuesta = "Se ha actualizado el estado del pedido de manera exitosa";
            //respuesta.setPedidos(pedidoActualizado);
            return new ResponseEntity<>(respuesta,HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(respuesta,HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            respuesta.setMensaje("No se encontró el id del producto");
            return new ResponseEntity<>(respuesta,HttpStatus.NOT_FOUND);
        }
    }


    /*
    @GetMapping("/")
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> listAll() {
    //public ResponseEntity<List<Categoria>> listAll(){
        try{
            List<EntityModel<Pedido>> pedidos = pedidoBusiness.listAll().stream()
                    .map(assemblerPedido::toModel)
                    .collect(Collectors.toList());
            CollectionModel<EntityModel<Pedido>> collection = CollectionModel.of(pedidos,
                    linkTo(methodOn(PedidoRestController.class).listAll()).withSelfRel());
            return new ResponseEntity<>(collection, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> show(@PathVariable Long id){
    //public ResponseEntity<Categoria> showCategory(@PathVariable Long id){
        try{
            Pedido pedido = pedidoBusiness.show(id);
            return new ResponseEntity<>(assemblerPedido.toModel(pedido),
                    HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody Pedido pedido){
        try{
            EntityModel<Pedido> entityModel = assemblerPedido.toModel(pedidoBusiness.save(pedido));
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        }catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> update(@PathVariable Long id, @RequestBody Pedido pedido){
        try {
            EntityModel<Pedido> entityModel = assemblerPedido.toModel(pedidoBusiness.update(id,pedido));
            //return new ResponseEntity<>(entityModel,HttpStatus.OK);
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try{
            pedidoBusiness.remove(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }*/
}
