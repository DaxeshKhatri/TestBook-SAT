package in.com.testbook.chatapp.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.com.testbook.chatapp.Fragments.ChatsFragment;
import in.com.testbook.chatapp.Fragments.FriendsFragment;
import in.com.testbook.chatapp.Fragments.UsersFragment;


public class SectionsPagerAdapter extends FragmentPagerAdapter{


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {

            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return  chatsFragment;

            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 2:
                UsersFragment usersFragment = new UsersFragment();
                return usersFragment;

            default:
                return  null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch (position) {
            case 0:
                return "CHATS";
            case 1:
                return "ACCEPTED FRIENDS";
            case 2:
                return "USERS";

            default:
                return null;
        }

    }

}
