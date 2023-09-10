package com.example.youcandoit_app.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youcandoit_app.R;
import com.example.youcandoit_app.dto.GroupDto;

import java.util.List;

public class DiyCertifyAdapter extends RecyclerView.Adapter<DiyCertifyAdapter.ViewHolder> {

    private List<GroupDto> dtoList;

    /** 클릭 이벤트 구현을 위해 인터페이스 선언 */
    public interface OnItemClickListener {
        /** 커스텀 ClickListener */
        void onItemClicked(int groupNumber);
    }

    // OnItemClickListener 참조 변수 선언
    private OnItemClickListener itemClickListener;

    /** 커스텀 ClickListener 전달 메소드 */
    public void setOnItemClickListener (OnItemClickListener listener) {
        itemClickListener = listener;
    }


    /** 뷰홀더 클래스 */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView gImage;
        private TextView gSubject, gName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gImage = itemView.findViewById(R.id.group_image);
            gSubject = itemView.findViewById(R.id.group_subject);
            gName = itemView.findViewById(R.id.group_name);
        }
    }


    public DiyCertifyAdapter(List<GroupDto> dtoList) {
        this.dtoList = dtoList;
    }

    /** 뷰홀더 객체 생성, 초기화 */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diy_certify_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        // RecycleView 영역을 클릭하게 되면 제일 먼저 이 메서드가 호출된다.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int groupNumber = 0;
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    groupNumber = dtoList.get(position).getGroup_number();
                    Log.i("DiyCertifyAdapter.java", "그룹번호 가져옴 : " + groupNumber);
                }
                itemClickListener.onItemClicked(groupNumber);
            }
        });

        return viewHolder;
    }

    /** 뷰홀더의 내용을 채운다. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupDto dto = dtoList.get(position);
        holder.gImage.setImageBitmap(dto.getGroup_image());
        holder.gSubject.setText(dto.getGroup_subject());
        holder.gName.setText(dto.getGroup_name());
    }

    /** 데이터의 갯수 리턴 */
    @Override
    public int getItemCount() {
        return dtoList.size();
    }
}
