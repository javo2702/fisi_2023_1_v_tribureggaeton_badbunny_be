package com.akinms.apirestful.controller;

import com.akinms.apirestful.business.IProductoBusiness;
import com.akinms.apirestful.entity.Producto;
import com.akinms.apirestful.responseentity.RespuestaMensaje;
import com.akinms.apirestful.responseentity.RespuestaProductos;
import com.akinms.apirestful.exception.BusinessException;
import com.akinms.apirestful.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/ne-gestion-productos/akinms/atencion-cliente/v1")
public class ProductoRestController {
    @Autowired
    private IProductoBusiness productoBusiness;


    /*
    @GetMapping("/")
    public ResponseEntity<RespuestaProductos> listAll() {
    //public ResponseEntity<List<Producto>> listAll() {
        RespuestaProductos respuesta = new RespuestaProductos();
        try{
            List<?> productos = productoBusiness.listAllProducts();
            if (productos.size()>0)
                respuesta.setMensaje("Se encontraron "+productos.size()+" productos");
            else
                respuesta.setMensaje("No se encontraron coincidencias");
            respuesta.setProductos(productos);
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/search2")
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listAllByName(@RequestParam String nombre) {
        try{
            List<EntityModel<Producto>> productos = productoBusiness.listAllProductsByName(nombre).stream()
                    .map(assemblerProducto::toModel)
                    .collect(Collectors.toList());
            CollectionModel<EntityModel<Producto>> collection = CollectionModel.of(productos,
                    linkTo(methodOn(ProductoRestController.class).listAllByName(nombre)).withSelfRel());
            return new ResponseEntity<>(collection, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
    @PostMapping("/productos/registrar")
    public ResponseEntity<RespuestaProductos> saveProduct(@RequestBody Producto producto){
        RespuestaProductos respuesta = new RespuestaProductos();
        respuesta.setMensaje("Error al guardar producto");
        try{
            Producto productoGuardado = productoBusiness.saveProduct(producto);
            respuesta.setMensaje("Producto guardado de manera exitosa");
            respuesta.setProductos(productoGuardado);
            return new ResponseEntity<>(respuesta,HttpStatus.CREATED);
        }catch (BusinessException e){
            return new ResponseEntity<>(respuesta,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/consultar-productos/{idProducto}")
    public ResponseEntity<RespuestaProductos> showProduct(@PathVariable Long idProducto){
        //public ResponseEntity<Producto> showProduct(@PathVariable Long id){
        RespuestaProductos respuesta = new RespuestaProductos();
        respuesta.setMensaje("Producto con id: "+idProducto+" no se encuentra registrado");
        try{
            Producto producto = productoBusiness.showProduct(idProducto);
            respuesta.setMensaje("Producto con id: "+idProducto);
            respuesta.setProductos(producto);
            return new ResponseEntity<>(respuesta,
                    HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(respuesta,HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/listar-productos/{idBodega}")
    public ResponseEntity<RespuestaProductos> listarProductosBodega(@PathVariable Long idBodega) {
        try{
            List<?> productos = productoBusiness.listProductsBodega(idBodega);
            RespuestaProductos respuesta = new RespuestaProductos("Productos por Bodega",productos);
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/buscar")
    public ResponseEntity<RespuestaProductos> listarProductosNombre(@RequestParam String nombre) {
        RespuestaProductos respuesta = new RespuestaProductos();
        try{
            List<?> productos = productoBusiness.listaProductsNombre(nombre);
            if (productos.size()>0)
                respuesta.setMensaje("Se encontraron "+productos.size()+" productos");
            else
                respuesta.setMensaje("No se encontraron coincidencias");
            respuesta.setProductos(productos);
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/consultar-productos/{idBodega}/buscar")
    public ResponseEntity<RespuestaProductos> listarProductosNombreBodega(@PathVariable Long idBodega, @RequestParam String nombre) {
        RespuestaProductos respuesta = new RespuestaProductos();
        try{
            List<?> productos = productoBusiness.listProductsNombreBodega(nombre,idBodega);
            if (productos.size()>0)
                respuesta.setMensaje("Se encontraron "+productos.size()+" productos");
            else
                respuesta.setMensaje("No se encontraron coincidencias");
            respuesta.setProductos(productos);
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/consultar-productos/{idBodega}/{idCategoria}")
    public ResponseEntity<RespuestaProductos> listarProductosCategoria(@PathVariable Long idCategoria, @PathVariable Long idBodega) {
        RespuestaProductos respuesta = new RespuestaProductos();
        try{
            List<Producto> productos = productoBusiness.listProductsCategory(idCategoria,idBodega);
            if (productos.size()>0)
                respuesta.setMensaje("Se encontraron "+productos.size()+" productos de la categoria " + productos.get(0).categoria.getNombre());
            else
                respuesta.setMensaje("La categoria no presenta productos registrados");
            respuesta.setProductos(productos);
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("actualizar/{idProducto}")
    public ResponseEntity<RespuestaProductos> actualizarProducto(@PathVariable Long idProducto, @RequestBody Producto producto){
        RespuestaProductos respuesta = new RespuestaProductos();
        try{
            Producto productoupdate = productoBusiness.actualizarProducto(idProducto,producto.getNombre(),producto.getStock(),producto.getPrecio());

            respuesta.setMensaje("Producto Actualizado de manera exitosa");
            respuesta.setProductos(productoBusiness.showProduct(idProducto));
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/eliminar/{idProducto}")
    public ResponseEntity<RespuestaMensaje> deleteProduct(@PathVariable Long idProducto){
        try{
            productoBusiness.removeProduct(idProducto);
            return new ResponseEntity<>(new RespuestaMensaje("Producto Eliminado de manera Exitosa"),HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
/*
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> showProduct(@PathVariable Long id){
    //public ResponseEntity<Producto> showProduct(@PathVariable Long id){
        try{
            Producto producto = productoBusiness.showProduct(id);
            return new ResponseEntity<>(assemblerProducto.toModel(producto),
                    HttpStatus.OK);
        } catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/")
    public ResponseEntity<?> saveProduct(@RequestBody Producto producto){
        try{
            EntityModel<Producto> entityModel = assemblerProducto.toModel(productoBusiness.saveProduct(producto));
            return ResponseEntity
                    .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(entityModel);
        }catch (BusinessException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> updateProduct(@PathVariable Long id, @RequestBody Producto producto){
        try {
            EntityModel<Producto> entityModel = assemblerProducto.toModel(productoBusiness.updateProducto(id,producto));
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
    */
}
