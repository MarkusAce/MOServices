package cl.rollerapp.backend.service;
import java.time.*;

public final class AnticipacionAgenda {
 private AnticipacionAgenda() {}
 public static Duration tiempoHabilEntre(LocalDateTime desde, LocalDateTime hasta) {
  Duration acumulado=Duration.ZERO;
  LocalDateTime cursor=desde;
  while(cursor.isBefore(hasta)) {
   LocalDateTime finDia=cursor.toLocalDate().plusDays(1).atStartOfDay();
   LocalDateTime fin=finDia.isBefore(hasta)?finDia:hasta;
   DayOfWeek dia=cursor.getDayOfWeek();
   if(dia!=DayOfWeek.SATURDAY&&dia!=DayOfWeek.SUNDAY) acumulado=acumulado.plus(Duration.between(cursor,fin));
   cursor=fin;
  }
  return acumulado;
 }
 public static boolean cumple(LocalDateTime desde, LocalDateTime hasta) {
  return tiempoHabilEntre(desde,hasta).compareTo(Duration.ofHours(48))>=0;
 }
}
