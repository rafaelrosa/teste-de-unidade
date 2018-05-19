package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.relogio.Relogio;

public class GeradorDePagamentoTest {
	@Test
	public void mustCreatePaymentToAFinishedAuction() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Avaliador avaliador = mock(Avaliador.class);
		
		Leilao leilao = new CriadorDeLeilao()
							.para("Playstation")
							.lance(new Usuario("Jose"), 2000.0)
							.lance(new Usuario("Maria"), 2500.0)
							.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(2500.0);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador);
		
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		
		// Mockito checks if the method was invoked and also
		// captures the object.
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
	}
	
	@Test
	public void mustCreatePaymentToAFinishedAuctionAndWithRealEvaluator() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Avaliador avaliador = new Avaliador(); 
		
		Leilao leilao = new CriadorDeLeilao()
							.para("Playstation")
							.lance(new Usuario("Jose"), 2000.0)
							.lance(new Usuario("Maria"), 2500.0)
							.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
//		when(avaliador.getMaiorLance()).thenReturn(2500.0);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador);
		
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		
		// Mockito checks if the method was invoked and also
		// captures the object.
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
	}
	
	@Test
	public void mustConsiderNextWorkingDayFromSaturday() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);
		
		Leilao leilao = new CriadorDeLeilao()
							.para("Playstation")
							.lance(new Usuario("Jose"), 2000.0)
							.lance(new Usuario("Maria"), 2500.0)
							.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		Calendar saturday = Calendar.getInstance();
		saturday.set(2018, Calendar.MAY, 19);
		
		when(relogio.hoje()).thenReturn(saturday);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		// ensures that the date of payment is next working day (Monday)
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));

		// ensures that the date of payment is 2 days after 2018-05-19 (21)
		assertEquals(21, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH) );
	}
	
	@Test
	public void mustConsiderNextWorkingDayFromSunday() {
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogio = mock(Relogio.class);
		
		Leilao leilao = new CriadorDeLeilao()
							.para("Playstation")
							.lance(new Usuario("Jose"), 2000.0)
							.lance(new Usuario("Maria"), 2500.0)
							.constroi();
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		
		Calendar domingo = Calendar.getInstance();
		domingo.set(2018, Calendar.MAY, 20);
		
		when(relogio.hoje()).thenReturn(domingo);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		// ensures that the date of payment is next working day (Monday)
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));

		// ensures that the date of payment is 1 day after 2018-05-20 (21)
		assertEquals(21, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH) );
	}
}
