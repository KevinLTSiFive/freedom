package src.main.scala.devices.APBSlaveUart
import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.apb._
import freechips.rocketchip.tilelink._
import sfc.blocks.ip.apbuart._
import sifive.blocks.devices.uart._
case class APBSlaveUartParams(
                             config : String,
                             raddress : BigInt,
                             lenth: BigInt
                             )

class APBUART(params:APBSlaveUartParams)(implicit p: Parameters) extends LazyModule {

  val blackboxName = params.config

  val dtsdevice = new SimpleDevice("APBUART",Seq("sfc,apbUart"))

  val cfg_apb_node = APBSlaveNode(  //instantiate a node of APBSlave
    Seq(
      APBSlavePortParameters(
        slaves = Seq(APBSlaveParameters(
          address       = Seq(AddressSet(params.raddress, params.lenth)),//0x8000L-1L
          resources     = dtsdevice.reg("control"),
          executable    = false,
          supportsWrite = true,
          supportsRead  = true)),
        beatBytes = 2)))

  val cfg_tl_node = cfg_apb_node := LazyModule(new TLToAPB).node


  override lazy val module = new LazyModuleImp(this){

    val u_apb_slave_uart = Module(new apb_uart(blackboxName))
    val (cfg, _) = cfg_apb_node.in(0)

    u_apb_slave_uart.io.PCLK := clock
    u_apb_slave_uart.io.UARTCLK := clock
    u_apb_slave_uart.io.PRESETn := ~reset
    u_apb_slave_uart.io.nUARTRST := ~reset

    u_apb_slave_uart.io.UARTRXDMACLR := Bool(false)
    u_apb_slave_uart.io.UARTTXDMACLR := Bool(false)

    u_apb_slave_uart.io.nUARTRI := Bool(false)
    u_apb_slave_uart.io.nUARTCTS := Bool(false)
    u_apb_slave_uart.io.nUARTDSR := Bool(false)
    u_apb_slave_uart.io.nUARTDCD := Bool(false)


    cfg.prdata := u_apb_slave_uart.io.PRDATA

    u_apb_slave_uart.io.PADDR := cfg.paddr(11,2)
    u_apb_slave_uart.io.PSEL  := cfg.psel
    u_apb_slave_uart.io.PENABLE := cfg.penable
    u_apb_slave_uart.io.PWRITE := cfg.pwrite
    u_apb_slave_uart.io.PWDATA := cfg.pwdata



  }

}

object APBUART