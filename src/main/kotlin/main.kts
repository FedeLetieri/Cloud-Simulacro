import java.lang.Exception

class proceso(
    val ideasProyecto:MutableList<Idea>,
    val distribucionDeDinero: MutableList<Distribucion>,
    val listaProyectos:MutableList<Proyecto>,
    val dineroDisponible:Int
) {

    fun proyectosElegidos(){

        val proyectos = ideasProyecto.map{it.seleccionarProyecto(listaProyectos)}
        validarCantidadProyectos(proyectos.count())
        return proyectos
    }

    fun validarCantidadProyectos(cantidad: Int){
        if(cantidad<2) throw Exception("Cantidad de Proyectos insuficiente")
    }

    fun distribucionesDeDinero() {
        validarDinero()
        return  distribucionDeDinero.map{ it.distribuirDinero(listaProyectos, dineroDisponible) }

    }

    fun validarDinero(){
        if(dineroDisponible<1000) throw Exception("Cantidad De dinero Insuficiente")
    }

}

interface Idea{
    abstract fun seleccionarProyecto(ListaProyectos: MutableList<Proyecto>): List<Proyecto>
}

class MasImpactoSocial(): Idea{
    override fun seleccionarProyecto(ListaProyectos : MutableList<Proyecto>): List<Proyecto> =
        mayoresProyectosConImpactoSocial(ListaProyectos).take(3)

    fun mayoresProyectosConImpactoSocial(ListaProyectos:MutableList<Proyecto> ) = ListaProyectos.sortedByDescending{it -> it.impactoSocial()}
}

class ConMasYMenosPlata(): Idea{
    override fun seleccionarProyecto(ListaProyectos: MutableList<Proyecto>): List<Proyecto> =
        List<Proyecto>(proyectoMasPLata(ListaProyectos),proyectoMenosPLata(ListaProyectos))

    fun proyectoMasPLata(ListaProyectos: MutableList<Proyecto>):Proyecto = ListaProyectos.maxBy { it -> it.cantidadDeDineroQueNecesita }

    fun proyectoMenosPLata(ListaProyectos: MutableList<Proyecto>):Proyecto = ListaProyectos.minBy { it -> it.cantidadDeDineroQueNecesita }

}

class Nacionales(): Idea{
    override fun seleccionarProyecto(ListaProyectos: MutableList<Proyecto>): List<Proyecto> =
        ListaProyectos.filter { it -> it.esNacional() }
}

class Combinatoria(val listaDeIdeas: MutableList<Idea>): Idea{
    override fun seleccionarProyecto(ListaProyectos: MutableList<Proyecto>): List<Proyecto> = listaDeIdeas.seleccionarProyecto(ListaProyectos)

    fun agregarIdea(idea:Idea) {listaDeIdeas.add(idea)}

    fun eliminarIdea(idea:Idea) {listaDeIdeas.remove(idea)}
}


interface Distribucion{
    abstract fun distribuirDinero(ListaProyectos: MutableList<Proyecto>,dineroDisponbile:Int)
}

class partesIguales():Distribucion{
    override fun distribuirDinero(ListaProyectos: MutableList<Proyecto>,dineroDisponbile:Int) {
        val dineroPorProyecto = dineroParaCadaProyecto(ListaProyectos,dineroDisponbile)

        ListaProyectos.forEach{it.recibirDinero(dineroPorProyecto)}
    }

    fun dineroParaCadaProyecto(ListaProyectos: MutableList<Proyecto>,dineroDisponbile:Int):Double = dineroDisponbile / ListaProyectos.count()

}

class mitadParaUnoYResto(): Distribucion{
    override fun distribuirDinero(ListaProyectos: MutableList<Proyecto>,dineroDisponbile:Int) {

        val primerProyecto = ListaProyectos.first()
        primerProyecto.recibirDinero(mitadDeDinero(dineroDisponbile))

        distribuirDineroAlresto(ListaProyectos,primerProyecto,dineroDisponbile)
    }

    fun distribuirDineroAlresto(ListaProyectos: MutableList<Proyecto>, primerProyecto:Proyecto,dineroDisponbile:Int){
        ListaProyectos.filter { it -> it !=primerProyecto }.
        forEach { it -> it.recibirDinero(distribucionRestoDeDinero(ListaProyectos,dineroDisponbile)) }

    }

    fun mitadDeDinero(dineroDisponbile:Int):Double = dineroDisponbile/2.0

    fun distribucionRestoDeDinero(ListaProyectos: MutableList<Proyecto>,dineroDisponbile:Int) =
        mitadDeDinero(dineroDisponbile) / ListaProyectos.count()
}


class AlAzar(): Distribucion{
    override fun distribuirDinero(ListaProyectos: MutableList<Proyecto>,dineroDisponbile:Int) {
        val primerProyectoAlAzar = ListaProyectos.random()
        val segundoProyectoAlAzar = ListaProyectos.filter { it -> it!=primerProyectoAlAzar }.random()
        primerProyectoAlAzar.recibirDinero(500.0)
        segundoProyectoAlAzar.recibirDinero(resto(dineroDisponbile))
    }
    fun resto(dineroDisponible:Int) = dineroDisponible - 500.0

}


abstract class Proyecto(
  val nombre:String,
  val descripcion:String,
  val cantidadDeDineroQueNecesita:Int,
  val datosBancarios:Datos,
  val listaDeResponsablesDelProyecto:MutableList<Persona>
){

    lateinit var dineroRecibido:Double

    fun impactoSocial() = costoImpactoFijo() + costoImpactoVariable()

    fun costoImpactoFijo() = 0.10*cantidadDeDineroQueNecesita

    abstract fun costoImpactoVariable():Double

    fun esNacional() = true

    fun recibirDinero(dinero:Double){dineroRecibido = dinero}
}

class Sociales(val fechaDeInicio:LocalDate,nombre:String,descripcion:String,
               cantidadDeDineroQueNecesita:Int,
               datosBancarios:Datos,listaDeResponsablesDelProyecto:MutableList<Persona>):
    Proyecto(nombre,descripcion,cantidadDeDineroQueNecesita,datosBancarios,listaDeResponsablesDelProyecto)
{

    override fun costoImpactoVariable() = 100 * cantidadDeAñosQUeLLevaCreada

    fun cantidadDeAñosQUeLLevaCreada() = chronoTime.between(LocalDate.now(),fechaDeInicio).year
    }

class Cooperativa(val socios:MutableList<Persona>,nombre:String,descripcion:String,
               cantidadDeDineroQueNecesita:Int,
               datosBancarios:Datos,listaDeResponsablesDelProyecto:MutableList<Persona>):
  Proyecto(nombre,descripcion,cantidadDeDineroQueNecesita,datosBancarios,listaDeResponsablesDelProyecto){

      companion object{
          val bonificacionDobleApellido = 45
          val bonificacionUnSoloApellido = 30
      }

      override fun costoImpactoVariable() = cantidadSociosConUnSoloApellido()* bonificacionUnSoloApellido +
                                            cantidadSociosConDobleApellido()* bonificacionDobleApellido

    fun cantidadSociosConDobleApellido() = (socios.filter{it.tieneDobleApellido()}).count()

    fun cantidadSociosConUnSoloApellido() = socios.count() - cantidadSociosConDobleApellido()
  }

class Ecologico(val area:Double,nombre:String,descripcion:String,
               cantidadDeDineroQueNecesita:Int,
               datosBancarios:Datos,listaDeResponsablesDelProyecto:MutableList<Persona>):
  Proyecto(nombre,descripcion,cantidadDeDineroQueNecesita,datosBancarios,listaDeResponsablesDelProyecto){

      override fun costoImpactoVariable() = area*10


}



class Persona(
  val nombre:String,
  val apellido:String,
  val direccion:String,
  val origen:a
){
    fun tieneDobleApellido():Boolean = this.apellido.contains(" ")
}



data class Datos(
  val dato1:String,
  val dato2:String,
  )