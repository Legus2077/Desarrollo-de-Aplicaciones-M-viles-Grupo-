package EMPRESA.ANDROID.DAO;

import EMPRESAANDROIDBEAN.ProductoBean;

public class ProductoDAO {

    private ProductoBean objProductoBean;

    public String CalcularOperacion(ProductoBean objProductoBean) {

        int marca = objProductoBean.getMarca();
        int talla = objProductoBean.getTalla();
        int numpares = objProductoBean.getNumpares();

        this.objProductoBean = new ProductoBean();

        this.objProductoBean.setMarca(marca);
        this.objProductoBean.setTalla(talla);
        this.objProductoBean.setNumpares(numpares);

        int costo = CalcularCostoParZapatillas(this.objProductoBean);
        this.objProductoBean.setCosto(costo);

        int venta = CalcularVenta(this.objProductoBean);
        this.objProductoBean.setVenta(venta);

        double descuento = CalcularDescuento(this.objProductoBean);
        this.objProductoBean.setDescuento(descuento);

        double ventaneta = CalcularVentaNeta(this.objProductoBean);
        this.objProductoBean.setVentaneta(ventaneta);

        String mensaje =
                "El costo del par de zapatillas: S/ " + costo + "\n" +
                        "La venta de las zapatillas: S/ " + venta + "\n" +
                        "El descuento: S/ " + descuento + "\n" +
                        "La venta neta: S/ " + ventaneta;

        return mensaje;
    }

    public int CalcularCostoParZapatillas(ProductoBean objProductoBean) {

        int costo = 0;

        switch (objProductoBean.getMarca()) {

            case 0: // NIKE
                switch (objProductoBean.getTalla()) {
                    case 0: // 36
                        costo = 150;
                        break;
                    case 1: // 37
                        costo = 160;
                        break;
                    case 2: // 38
                        costo = 160;
                        break;
                    case 3: // 39
                        costo = 160;
                        break;
                    case 4: // 40
                        costo = 160;
                        break;
                    case 5: // 41
                        costo = 160;
                        break;
                    case 6: // 42
                        costo = 160;
                        break;
                }
                break;

            case 1: // ADIDAS
                switch (objProductoBean.getTalla()) {
                    case 0:
                        costo = 140;
                        break;
                    case 1:
                        costo = 150;
                        break;
                    case 2:
                        costo = 150;
                        break;
                    case 3:
                        costo = 150;
                        break;
                    case 4:
                        costo = 150;
                        break;
                    case 5:
                        costo = 150;
                        break;
                    case 6:
                        costo = 150;
                        break;
                }
                break;

            case 2: // PUMA
                switch (objProductoBean.getTalla()) {
                    case 0:
                        costo = 80;
                        break;
                    case 1:
                        costo = 85;
                        break;
                    case 2:
                        costo = 90;
                        break;
                    case 3:
                        costo = 90;
                        break;
                    case 4:
                        costo = 90;
                        break;
                    case 5:
                        costo = 90;
                        break;
                    case 6:
                        costo = 90;
                        break;
                }
                break;

            case 3: // REEBOK
                switch (objProductoBean.getTalla()) {
                    case 0:
                        costo = 100;
                        break;
                    case 1:
                        costo = 105;
                        break;
                    case 2:
                        costo = 110;
                        break;
                    case 3:
                        costo = 110;
                        break;
                    case 4:
                        costo = 110;
                        break;
                    case 5:
                        costo = 110;
                        break;
                    case 6:
                        costo = 110;
                        break;
                }
                break;
        }

        return costo;
    }

    public int CalcularVenta(ProductoBean objProductoBean) {

        int venta;

        venta = objProductoBean.getCosto()
                * objProductoBean.getNumpares();

        return venta;
    }

    public double CalcularDescuento(ProductoBean objProductoBean) {

        double descuento = 0;

        if (objProductoBean.getNumpares() >= 2
                && objProductoBean.getNumpares() <= 5) {

            descuento = 0.05 * objProductoBean.getVenta();

        } else if (objProductoBean.getNumpares() >= 6
                && objProductoBean.getNumpares() <= 10) {

            descuento = 0.08 * objProductoBean.getVenta();

        } else if (objProductoBean.getNumpares() >= 11
                && objProductoBean.getNumpares() <= 20) {

            descuento = 0.10 * objProductoBean.getVenta();

        } else if (objProductoBean.getNumpares() > 20) {

            descuento = 0.15 * objProductoBean.getVenta();
        }

        return descuento;
    }

    public double CalcularVentaNeta(ProductoBean objProductoBean) {

        double ventaneta;

        ventaneta = objProductoBean.getVenta()
                - objProductoBean.getDescuento();

        return ventaneta;
    }
}


