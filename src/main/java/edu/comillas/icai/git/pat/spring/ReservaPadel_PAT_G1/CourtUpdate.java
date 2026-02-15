package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

public record CourtUpdate (
        String nombre,
        String ubicacion,
        Float precio,
        Boolean activa
)
{
    public boolean isEmpty(){
        return (nombre==null)&(ubicacion==null)&(precio==null&(activa==null));
    }
}
