import org.denigma.threejs._
import org.denigma.threejs.extensions.Container3D
import org.denigma.threejs.extensions.controls.{CameraControls, JumpCameraControls}
import org.denigma.threejs.extras.HtmlSprite
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.raw.{HTMLElement, HTMLTextAreaElement}

import scala.scalajs.js
import scala.util.Random
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement

object ThreeJsDemo {

  def activate(): Unit = {
    val el: HTMLElement = dom.document.getElementById("container").asInstanceOf[HTMLElement]
    val demo = new ExampleScene(el, 1280, 500) // scalastyle:ignore
    demo.render()
  }
}

// scalastyle:off
class ExampleScene(val container: HTMLElement, val width: Double, val height: Double) extends Container3D {
  val geometry = new BoxGeometry(350, 300, 250)

  val colors = List("green", "red", "blue", "orange", "purple", "teal")
  val colorMap = Map(colors.head -> 0xA1CF64, colors(1) -> 0xD95C5C, colors(2) -> 0x6ECFF5,
    colors(3) -> 0xF05940, colors(4) -> 0x564F8A, colors.tail -> 0x00B5AD)

  def materialParams(name: String): MeshLambertMaterialParameters = js.Dynamic.literal(
    color = new Color(colorMap(name)) // wireframe = true
  ).asInstanceOf[MeshLambertMaterialParameters]

  def randColorName: String = colors(Random.nextInt(colors.size))

  var meshes = addMesh(new Vector3(0, 0, 0)) :: addMesh(new Vector3(400, 0, 200)) :: addMesh(new Vector3(-400, 0, 200)) :: Nil

  override val controls: CameraControls = new ExampleControls(camera, this.container, scene, width, height, this.meshes.head.position.clone())

  val light = new DirectionalLight(0xffffff, 2)
  light.position.set(1, 1, 1).normalize()
  scene.add(light)

  def addMesh(pos: Vector3 = new Vector3()): Mesh = {
    val material = new MeshLambertMaterial(this.materialParams(randColorName))
    val mesh: Mesh = new Mesh(geometry, material)
    mesh.name = pos.toString
    mesh.position.set(pos.x, pos.y, pos.z)
    mesh
  }

  meshes.foreach(scene.add)
}

// scalastyle: on

/**
  * Just shows that some effects are working
  * @param cam the camera control
  * @param el the html element
  * @param sc scene
  * @param center center of screen
  */
class ExampleControls(cam: Camera, el: HTMLElement, sc: Scene, width: Double, height: Double,
  center: Vector3 = new Vector3()) extends JumpCameraControls(cam, el, sc, width, height, center) {
  import org.querki.jquery._

  lazy val $el = $(el)

  override def onMouseMove(event: MouseEvent) = {
    val (offsetX, offsetY) = ($el.offset().left, $el.offset().top)
    this.onCursorMove(event.clientX - offsetX, event.clientY - offsetY, width, height)

    enter.keys.foreach {
      case m: Mesh =>
        m.material match {
          case mat: MeshLambertMaterial => mat.wireframe = true
          case _ => // do nothing
        }

      case _ => // do nothing
    }

    exit.keys.foreach {
      case m: Mesh =>
        m.material match {
          case mat: MeshLambertMaterial => mat.wireframe = false
          case _ => // do nothing
        }

      case _ => // do nothing
    }

    rotateOnMove(event)

  }

}
