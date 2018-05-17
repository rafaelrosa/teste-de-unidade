package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {
	
	private EnviadorDeEmail carteiroFalso;
	private RepositorioDeLeiloes daoFalso;
	private EncerradorDeLeilao encerrador;
	
	@Before
	public void setup() {
		carteiroFalso = mock(EnviadorDeEmail.class);
		daoFalso = mock(LeilaoDao.class);
		encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
	}
	
	@Test
	public void finishAuctionThatHasStartedLastWeek() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		daoFalso.salva(leilao1);
		daoFalso.salva(leilao2);
		
		encerrador.encerra();
		
		// Best practice to test list: check length
		assertEquals(2, encerrador.getTotalEncerrados());
		
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}
	
	@Test
	public void dontFinishAuctionThatHasStartedYesterday() {
		Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		encerrador.encerra();
		
		// Best practice to test list: check length
		assertEquals(0, encerrador.getTotalEncerrados());
		
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());
	}
	
	@Test
	public void doNothingWhenThereAreNoAuctions() {
		RepositorioDeLeiloes daoFalso = mock(LeilaoDao.class);
		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		
		encerrador.encerra();
		
		// Best practice to test list: check length
		assertEquals(0, encerrador.getTotalEncerrados());
	}
	
	@Test
	public void updateFinishedActions() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		encerrador.encerra();
		
		// fail the test if this method is not invoked
		// with times 1, we want only 1 invokation
		// and must be called with leilao1
		verify(daoFalso, times(1)).atualiza(leilao1);
	}
	
	@Test
    public void mustNotFinishAuctionsThatStartedLessThanOneWeekBefore() {
        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
        
        verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
    }
	
	@Test
	public void checkIfEmailIsSentWithAuctionInfo() {
		Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -8);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();

        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

        encerrador.encerra();

        assertEquals(1, encerrador.getTotalEncerrados());
        assertTrue(leilao1.isEncerrado());
        
        // mocks to be analised
        InOrder inOrder = inOrder(daoFalso, carteiroFalso);
        
        // 1st check
        inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
        
        // 2nd check
        inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
        
	}
}
